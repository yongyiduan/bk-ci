<template>
    <article class="build-detail-home">
        <header class="build-detail-header section-box">
            <bk-breadcrumb class="header-bread" separator-class="bk-icon icon-angle-right">
                <bk-breadcrumb-item v-for="(item,index) in navList" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>
            <bk-tab :active.sync="active" type="unborder-card" class="header-tab" @tab-change="changeTab">
                <bk-tab-panel
                    v-for="(panel, index) in panels"
                    v-bind="panel"
                    :key="index">
                </bk-tab-panel>
            </bk-tab>
        </header>
        <router-view class="build-detail-main section-box"></router-view>
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
                    { label: 'Report', name: 'buildReports' },
                    { label: 'Configuration', name: 'buildConfig' }
                ],
                active: 'buildDetail'
            }
        },

        computed: {
            ...mapState(['curPipeline', 'projectId']),

            navList () {
                return [
                    { title: this.curPipeline.displayName, link: { name: 'buildList' } },
                    { title: '# ' + this.$route.params.buildNum }
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
                    const title = gitRequestEvent.commitMsg + ' #' + this.$route.params.buildNum
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
        padding-left: 25px;
    }
    .build-detail-header {
        background: #fff;
        .header-bread {
            line-height: 30px;
            padding: 11px 25px 0;
        }
        .header-tab {
            /deep/ .bk-tab-header {
                padding: 0 14px;
            }
            /deep/ .bk-tab-section {
                padding: 0;
            }
        }
    }
    .build-detail-main {
        margin-top: 25px;
        height: calc(100% - 116px);
        overflow: auto;
        background: #fff;
    }
</style>
