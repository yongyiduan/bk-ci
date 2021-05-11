/**
 * @file router 配置
 * @author Blueking
 */

import Vue from 'vue'
import VueRouter from 'vue-router'
Vue.use(VueRouter)

const NotFound = () => import(/* webpackChunkName: 'none' */'@/views/404')
const pipelines = () => import(/* webpackChunkName: 'pipelines' */'@/views/pipelines')
const pipelineDetail = () => import(/* webpackChunkName: 'pipelinesDetail' */'@/views/pipeline-detail/index')
const buildDetail = () => import(/* webpackChunkName: 'pipelinesDetail' */'@/views/pipeline-detail/detail')
const buildArtifacts = () => import(/* webpackChunkName: 'pipelinesDetail' */'@/views/pipeline-detail/artifacts')
const buildReports = () => import(/* webpackChunkName: 'pipelinesDetail' */'@/views/pipeline-detail/reports')
const buildConfig = () => import(/* webpackChunkName: 'pipelinesDetail' */'@/views/pipeline-detail/config')
const setting = () => import(/* webpackChunkName: 'setting' */'@/views/setting/index')
const basicSetting = () => import(/* webpackChunkName: 'setting' */'@/views/setting/basic')
const credentialList = () => import(/* webpackChunkName: 'setting' */'@/views/setting/credential')
const agentPools = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/index')
const addAgent = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/add-agent')
const agentList = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/agent-list')
const agentDetail = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/agent-detail')

const routes = [
    {
        path: '/',
        name: 'pipelines',
        component: pipelines
    },
    {
        path: '/detail',
        name: 'pipelineDetail',
        component: pipelineDetail,
        children: [
            {
                path: '/',
                name: 'buildDetail',
                component: buildDetail
            },
            {
                path: '/artifacts',
                name: 'buildArtifacts',
                component: buildArtifacts
            },
            {
                path: '/reports',
                name: 'buildReports',
                component: buildReports
            },
            {
                path: '/config',
                name: 'buildConfig',
                component: buildConfig
            }
        ]
    },
    {
        path: '/setting',
        name: 'setting',
        component: setting,
        children: [
            {
                path: '/',
                name: 'basicSetting',
                component: basicSetting
            },
            {
                path: '/credential',
                name: 'credentialList',
                component: credentialList
            },
            {
                path: '/agent-pools',
                name: 'agentPools',
                component: agentPools
            },
            {
                path: '/add-agent/:pool',
                name: 'addAgent',
                component: addAgent
            },
            {
                path: '/agent-list',
                name: 'agentList',
                component: agentList
            },
            {
                path: '/agent-detail',
                name: 'agentDetail',
                component: agentDetail
            }
        ]
    },
    {
        path: '*',
        name: '404',
        component: NotFound
    }
]

const router = new VueRouter({
    mode: 'history',
    routes: routes
})

export default router
