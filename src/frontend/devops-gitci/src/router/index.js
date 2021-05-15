/**
 * @file router 配置
 * @author Blueking
 */

import Vue from 'vue'
import VueRouter from 'vue-router'
Vue.use(VueRouter)

const main = () => import(/* webpackChunkName: 'entry' */'@/views/index')
const notFound = () => import(/* webpackChunkName: 'entry' */'@/views/404')
const notifications = () => import(/* webpackChunkName: 'notifications' */'@/views/notifications')
const pipeline = () => import(/* webpackChunkName: 'pipelines' */'@/views/pipeline')
const buildList = () => import(/* webpackChunkName: 'pipelines' */'@/views/pipeline/build-list')
const pipelineDetail = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/index')
const buildDetail = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/detail')
const buildArtifacts = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/artifacts')
const buildReports = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/reports')
const buildConfig = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/config')
const setting = () => import(/* webpackChunkName: 'setting' */'@/views/setting/index')
const basicSetting = () => import(/* webpackChunkName: 'setting' */'@/views/setting/basic')
const credentialList = () => import(/* webpackChunkName: 'setting' */'@/views/setting/credential')
const agentPools = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/index')
const addAgent = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/add-agent')
const agentList = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/agent-list')
const agentDetail = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/agent-detail')

const routes = [
    {
        path: '/:projectId',
        component: main,
        children: [
            {
                path: '',
                component: pipeline,
                children: [
                    {
                        path: '',
                        name: 'buildList',
                        component: buildList
                    },
                    {
                        path: 'detail/:buildId',
                        name: 'pipelineDetail',
                        component: pipelineDetail,
                        children: [
                            {
                                path: '',
                                name: 'buildDetail',
                                component: buildDetail
                            },
                            {
                                path: 'artifacts',
                                name: 'buildArtifacts',
                                component: buildArtifacts
                            },
                            {
                                path: 'reports',
                                name: 'buildReports',
                                component: buildReports
                            },
                            {
                                path: 'config',
                                name: 'buildConfig',
                                component: buildConfig
                            }
                        ]
                    }
                ]
            },
            {
                path: 'setting',
                name: 'setting',
                component: setting,
                children: [
                    {
                        path: '',
                        name: 'basicSetting',
                        component: basicSetting
                    },
                    {
                        path: 'credential',
                        name: 'credentialList',
                        component: credentialList
                    },
                    {
                        path: 'agent-pools',
                        name: 'agentPools',
                        component: agentPools
                    },
                    {
                        path: 'add-agent/:pool',
                        name: 'addAgent',
                        component: addAgent
                    },
                    {
                        path: 'agent-list',
                        name: 'agentList',
                        component: agentList
                    },
                    {
                        path: 'agent-detail',
                        name: 'agentDetail',
                        component: agentDetail
                    }
                ]
            },
            {
                path: 'notifications',
                name: 'notifications',
                component: notifications
            },
            {
                path: '*',
                name: '404',
                component: notFound
            }
        ]
    }
]

const router = new VueRouter({
    mode: 'history',
    routes: routes
})

export default router
