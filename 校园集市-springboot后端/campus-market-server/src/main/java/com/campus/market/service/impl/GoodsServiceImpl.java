package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.dto.GoodsPublishDTO;
import com.campus.market.entity.Favorite;
import com.campus.market.entity.Goods;
import com.campus.market.entity.SchoolLocation;
import com.campus.market.entity.User;
import com.campus.market.mapper.FavoriteMapper;
import com.campus.market.mapper.GoodsMapper;
import com.campus.market.mapper.SchoolLocationMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 闲置商品服务实现
 */
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final SchoolLocationMapper schoolLocationMapper;

    @Override
    public PageResult<Goods> list(Integer categoryId, String keyword, int page, int size) {
        QueryWrapper<Goods> qw = new QueryWrapper<>();
        qw.eq("status", 1); // 只在售
        if (categoryId != null) {
            qw.eq("category_id", categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            // TODO: 数据量大后迁移 FULLTEXT 索引 / Elasticsearch（文档 3.4 已预留全文索引）
            qw.and(w -> w.like("title", keyword).or().like("description", keyword));
        }
        qw.orderByDesc("create_time");
        Page<Goods> p = page(new Page<>(page, size), qw);
        fillSellerInfo(p.getRecords());
        return PageResult.of(p);
    }

    @Override
    public Goods detail(Long id) {
        // 服务端累计浏览数
        baseMapper.incrementView(id);
        Goods goods = getById(id);
        if (goods == null) {
            throw new BizException("商品不存在或已删除");
        }
        fillSellerInfo(List.of(goods));
        if (goods.getLocationId() != null) {
            SchoolLocation loc = schoolLocationMapper.selectById(goods.getLocationId());
            if (loc != null) {
                goods.setLocationName(loc.getName());
            }
        }
        return goods;
    }

    @Override
    public Goods publish(Long userId, GoodsPublishDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        // 信用分低于 60 限制发布
        if (user.getCreditScore() != null && user.getCreditScore() < 60) {
            throw new BizException("信用分不足 60，暂时不能发布商品");
        }
        Goods goods = new Goods();
        goods.setUserId(userId);
        goods.setTitle(dto.getTitle());
        goods.setCategoryId(dto.getCategoryId());
        goods.setPrice(dto.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setQuality(dto.getQuality() != null ? dto.getQuality() : 3);
        goods.setDescription(dto.getDescription());
        goods.setImages(dto.getImages());
        goods.setLocationId(dto.getLocationId());
        // TODO: 接入内容审核后先置 0(审核中)，骨架阶段直接在售
        goods.setStatus(1);
        goods.setViewCount(0);
        goods.setFavCount(0);
        goods.setWantCount(0);
        save(goods);
        return goods;
    }

    @Override
    public PageResult<Goods> my(Long userId, Integer status, int page, int size) {
        QueryWrapper<Goods> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        return PageResult.of(page(new Page<>(page, size), qw));
    }

    @Override
    public void offShelf(Long userId, Long id) {
        Goods goods = requireOwnGoods(userId, id);
        if (goods.getStatus() != 1) {
            throw new BizException("当前状态不能下架");
        }
        goods.setStatus(3); // 卖家下架
        updateById(goods);
    }

    @Override
    public void onShelf(Long userId, Long id) {
        Goods goods = requireOwnGoods(userId, id);
        if (goods.getStatus() != 3) {
            throw new BizException("仅已下架的商品可重新上架");
        }
        goods.setStatus(1);
        updateById(goods);
    }

    /**
     * 收藏/取消收藏（切换式）
     * uk_user_goods 唯一索引防重复收藏，并发双击由唯一索引兜底
     */
    @Override
    public Map<String, Object> toggleFavorite(Long userId, Long id) {
        Goods goods = getById(id);
        if (goods == null) {
            throw new BizException("商品不存在或已删除");
        }
        Favorite exist = favoriteMapper.selectOne(new QueryWrapper<Favorite>()
                .eq("user_id", userId).eq("goods_id", id));
        boolean favorited;
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            baseMapper.changeFavCount(id, -1);
            favorited = false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setGoodsId(id);
            try {
                favoriteMapper.insert(fav);
                baseMapper.changeFavCount(id, 1);
            } catch (DuplicateKeyException e) {
                // 并发重复点击：唯一索引兜底，按已收藏处理（幂等）
            }
            favorited = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        return result;
    }

    @Override
    public PageResult<Goods> favoriteMy(Long userId, int page, int size) {
        Page<Favorite> fp = favoriteMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<Favorite>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));
        // 基础实现：逐条取商品（TODO: 数据量大后改联表/批量查询）
        List<Goods> goodsList = fp.getRecords().stream()
                .map(f -> getById(f.getGoodsId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        fillSellerInfo(goodsList);
        return PageResult.of(goodsList, fp.getTotal());
    }

    /** 取商品并校验是当前用户本人发布的 */
    private Goods requireOwnGoods(Long userId, Long id) {
        Goods goods = getById(id);
        if (goods == null) {
            throw new BizException("商品不存在或已删除");
        }
        if (!goods.getUserId().equals(userId)) {
            throw new BizException("只能操作自己发布的商品");
        }
        return goods;
    }

    /** 批量填充卖家昵称/头像 */
    private void fillSellerInfo(List<Goods> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = list.stream().map(Goods::getUserId).distinct().collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        for (Goods g : list) {
            User u = userMap.get(g.getUserId());
            if (u != null) {
                g.setSellerNickname(u.getNickname());
                g.setSellerAvatar(u.getAvatar());
            }
        }
    }
}
