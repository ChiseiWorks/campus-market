import { createStore } from 'vuex'

/** 用户模块：登录态 + 认证状态 */
const user = {
  namespaced: true,
  state: {
    token: '',
    userInfo: null
  },
  getters: {
    isLogin: (state) => !!state.token,
    // 认证状态：0未认证 1审核中 2已认证 3已驳回
    authStatus: (state) => (state.userInfo ? state.userInfo.authStatus : 0),
    isAuthed: (state, getters) => getters.authStatus === 2,
    isRunner: (state) => !!(state.userInfo && state.userInfo.isRunner === 1),
    creditScore: (state) => (state.userInfo ? state.userInfo.creditScore : 100)
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      uni.setStorageSync('token', token)
    },
    SET_USER_INFO(state, info) {
      state.userInfo = info
      uni.setStorageSync('userInfo', JSON.stringify(info))
    },
    LOGOUT(state) {
      state.token = ''
      state.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    }
  },
  actions: {
    /** 登录成功后调用 */
    async login({ commit }, { token, userInfo }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
    },
    logout({ commit }) {
      commit('LOGOUT')
      uni.reLaunch({ url: '/pages/user/login' })
    }
  }
}

export default createStore({
  modules: { user }
})
