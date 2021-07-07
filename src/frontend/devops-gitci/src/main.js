/**
 * @file main entry
 * @author <%- author %>
 */

import '../build/public-path'
import Vue from 'vue'

import App from '@/App'
import router from '@/router'
import store from '@/store'
import '@/css/index.css'
import '@/common/bkmagic'
import '@icon-cool/bk-icon-gitci/src/index'
import icon from '@/components/icon'
import log from '@blueking/log'
import VeeValidate from 'vee-validate'

Vue.component('icon', icon)
Vue.use(log)
Vue.use(VeeValidate)

window.mainComponent = new Vue({
    el: '#app',
    router,
    store,
    components: { App },
    template: '<App/>'
})
