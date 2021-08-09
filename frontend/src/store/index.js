import Vue from 'vue';
import Vuex from 'vuex';

import banner from './banner/index';
import product from './product/index';
import cart from './cart/index';
import blog from './blog/index';
import insta from './insta/index';
import mypage from './mypage/index';
import wanted from './wanted/index';
import lookatme from './lookatme/index';
import createPersistedState from 'vuex-persistedstate';
import Axios from 'axios';
import Http from '../http.js';
import VueRouter from 'vue-router';

Vue.use(Vuex);
const DEVELOPMODE = true
export default new Vuex.Store({
  modules: {
    banner,
    product,
    cart,
    blog,
    insta,
    mypage,
    wanted,
    lookatme,
  },
  plugins: [createPersistedState()],
  getters: {
    fileURL: function () {
      return DEVELOPMODE ? 'http://localhost:8080/': 'http://profileglance.site/'
    },
    DEVELOPMODE: function () {
      return DEVELOPMODE
    }
  },
  state: {
    // 개발모드면 true 배포모드면 false
    // DEVELOPMODE: DEVELOPMODE,
    token: '',
    // 0: 관리자, 1: 일반유저, 2: 기업유저
    userType: 0,
    userId: 1,
    data: {
      userData: {
        userEmail: '',
        userName: '',
        userNickname: '',
        userBirth: '',
        major1: '',
        major2: '',
        countLike: 0,
        countVideo: 0,
        portfolio1: '',
        portfolio2: '',
        userImg: '',
      },
      companyData: {
        companyId: '',
        companyEmail: '',
        companyName: '',
        companyPhone: '',
        companyImg: '',
      },
    },
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
    },
    DELETE_TOKEN(state) {
      state.token = '';
    },
    UPDATE_USER_INFO(state, userData) {
      state.data.userData = userData;
      if (userData.admin) {
        state.userType = 0;
      } else {
        state.userType = 1;
      }
    },
    UPDATE_COMPANY_INFO(state, companyData) {
      state.data.companyData = companyData;
      state.userType = 2;
    },
    REQUEST_LOGOUT(state) {
      console.log('requestlogout');
      state.data = {
        userData: {
          userEmail: '',
          userName: '',
          userNickname: '',
          userBirth: '',
          major1: '',
          major2: '',
          countLike: 0,
          countVideo: 0,
          portfolio1: '',
          portfolio2: '',
          userImg: '',
        },
        companyData: {
          companyId: '',
          companyEmail: '',
          companyName: '',
          companyPhone: '',
          companyImg: '',
        },
      };
      state.token = 1;
      state.userType = 1;
    },
  },
  actions: {
    setToken({ commit }, token) {
      commit('SET_TOKEN', token);
    },
    updateUserInfo({ commit }, userData) {
      commit('UPDATE_USER_INFO', userData);
    },
    updateCompanyInfo({ commit }, companyData) {
      commit('UPDATE_COMPANY_INFO', companyData);
    },
    requestDeleteUser({ commit }) {
      console.log('delete');
      Http.delete('/user/delete/' + 'test2@test.com')
        .then((res) => {
          console.log('then');
          commit('DELETE_TOKEN');
          localStorage.removeItem('user_token');
          location.href = 'http://localhost:8080';
        })
        .catch((err) => {
          console.log('catch');
          console.log(err);
        });
    },
    requestLogout({ commit }) {
      commit('REQUEST_LOGOUT');
    },
  },
});
