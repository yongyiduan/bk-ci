/**
 * @file main entry
 * @author <%- author %>
 */

import '../build/public-path'
import Vue from 'vue'

import App from '@/App'
import router from '@/router'
import store from '@/store'
import '@/common/bkmagic'
import '@icon-cool/bk-icon-gitci/src/index'
import icon from '@/components/icon'
import log from '@blueking/log'

Vue.component('icon', icon)
Vue.use(log)

global.mainComponent = new Vue({
    el: '#app',
    router,
    store,
    components: { App },
    template: '<App/>'
})
