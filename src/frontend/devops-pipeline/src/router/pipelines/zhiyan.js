// 智研交付流流水线定制页面
const zyPipelinesEntry = () => import(/* webpackChunkName: "zyPipeline" */'../../views/zhiyan/index.vue')
const zyPipelinesDetail = () => import(/* webpackChunkName: "zyPipeline" */'../../views/zhiyan/exec_detail.vue')
const zyPipelinesEdit = () => import(/* webpackChunkName: "zyPipeline" */'../../views/zhiyan/edit.vue')
const zyPipelinesPreview = () => import(/* webpackChunkName: "zyPipeline" */'../../views/zhiyan/preview.vue')

export default {
    path: ':pipelineId',
    component: zyPipelinesEntry,
    name: 'zyPipelines',
    children: [
        {
            // 详情
            path: 'detail/:buildNo/:type?',
            name: 'zyPipelinesDetail',
            component: zyPipelinesDetail,
            meta: {
                title: 'pipeline',
                header: 'pipeline',
                icon: 'pipeline',
                to: 'pipelinesList'
            }
        },
        {
            // 流水线编辑
            path: 'edit/:tab?',
            name: 'zyPipelinesEdit',
            meta: {
                icon: 'pipeline',
                title: 'pipeline',
                header: 'pipeline',
                to: 'pipelinesList'
            },
            component: zyPipelinesEdit
        },
        {
            // 流水线执行可选插件
            path: 'preview',
            name: 'zyPipelinesPreview',
            meta: {
                icon: 'pipeline',
                title: 'pipeline',
                header: 'pipeline',
                to: 'pipelinesList'
            },
            component: zyPipelinesPreview
        }
    ]
}
