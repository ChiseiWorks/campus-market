<script>
export default {
  onLaunch() {
    // 启动时恢复登录态
    const token = uni.getStorageSync('token')
    const userInfo = uni.getStorageSync('userInfo')
    if (token && userInfo) {
      this.$store.commit('user/SET_TOKEN', token)
      this.$store.commit('user/SET_USER_INFO', JSON.parse(userInfo))
    }
    console.log('校园集市 App 启动')
  },
  onShow() {},
  onHide() {}
}
</script>

<style>
/* 全局样式：淘宝式浅灰底 + 主题橙 */
page {
  background-color: #F5F5F5;
  font-size: 28rpx;
  color: #323233;
}
</style>
