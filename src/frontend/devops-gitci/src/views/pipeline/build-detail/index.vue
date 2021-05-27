<template>
    <article class="build-detail-home">
        <section class="section-box build-detail-header">
            <bk-breadcrumb class="build-detail-crumb" separator-class="bk-icon icon-angle-right">
                <bk-breadcrumb-item v-for="(item,index) in navList" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>

            <bk-tab :active.sync="active" type="unborder-card" class="header-tab" @tab-change="changeTab">
                <bk-tab-panel
                    v-for="(panel, index) in panels"
                    v-bind="panel"
                    :key="index">
                </bk-tab-panel>
            </bk-tab>
        </section>

        <router-view class="section-box build-detail-main"></router-view>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { modifyHtmlTitle } from '@/utils'
    import { pipelines } from '@/http'

    export default {
        data () {
            return {
                panels: [
                    { label: 'Build detail', name: 'buildDetail' },
                    { label: 'Artifacts', name: 'buildArtifacts' },
                    { label: 'Reports', name: 'buildReports' },
                    { label: 'Configuration', name: 'buildConfig' }
                ],
                active: 'buildDetail',
                buildNum: ''
            }
        },

        computed: {
            ...mapState(['curPipeline', 'projectId']),

            navList () {
                return [
                    { title: this.curPipeline.displayName, link: { name: 'buildList' } },
                    { title: '# ' + this.buildNum }
                ]
            }
        },

        watch: {
            '$route.name': {
                handler (name) {
                    this.active = name
                    this.changeTab(name)
                },
                immediate: true
            }
        },

        created () {
            this.setHtmlTitle()
        },

        methods: {
            changeTab (name) {
                this.$router.push({ name })
            },

            setHtmlTitle () {
                const params = {
                    pipelineId: this.$route.params.pipelineId,
                    buildId: this.$route.params.buildId
                }
                return pipelines.getPipelineBuildDetail(this.projectId, params).then((res) => {
                    const gitRequestEvent = res.gitRequestEvent || {}
                    const modelDetail = res.modelDetail || {}
                    this.buildNum = modelDetail.buildNum
                    const title = gitRequestEvent.commitMsg + ' #' + this.buildNum
                    modifyHtmlTitle(title)
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .build-detail-home {
        padding-left: 20px;
    }
    .build-detail-header {
        background: #fff;
    }
    .build-detail-crumb {
        height: 50px;
        line-height: 50px;
        padding: 0 27px;
    }
    .header-tab {
        margin-top: -10px;
        /deep/ .bk-tab-header {
            padding: 0 14px;
            background-image: none !important;
        }
        /deep/ .bk-tab-section {
            padding: 0;
        }
    }
    .build-detail-main {
        margin-top: 20px;
        height: calc(100% - 120px);
        background: #fff;
    }
</style>
