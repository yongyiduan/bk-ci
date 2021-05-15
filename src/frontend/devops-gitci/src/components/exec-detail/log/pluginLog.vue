<template>
    <section class="plugin-log">
        <bk-log-search :down-load-link="downLoadLink" :execute-count="executeCount" @change-execute="changeExecute" class="log-tools"></bk-log-search>
        <bk-log class="bk-log" ref="scroll" @tag-change="tagChange"></bk-log>
    </section>
</template>

<script>
    import { uuid } from '@/utils'
    import { pipelines } from '@/http'
    export default {
        props: {
            plugin: Object,
            stageIndex: Number,
            jobIndex: Number
        },

        data () {
            return {
                postData: {
                    projectId: this.projectId,
                    pipelineId: this.pipelineId,
                    buildId: this.buildId,
                    tag: this.plugin.id,
                    subTag: '',
                    currentExe: this.plugin.executeCount,
                    lineNo: 0
                },
                timeId: '',
                clearIds: []
            }
        },

        computed: {
            projectId () {
                return this.$route.params.projectId
            },

            pipelineId () {
                return this.$route.params.pipelineId
            },

            buildId () {
                return this.$route.params.buildId
            },

            downLoadLink () {
                const fileName = encodeURI(encodeURI(`${this.stageIndex + 1}-${this.jobIndex + 1}-${this.pluginIndex + 1}-${this.plugin.name}`))
                const tag = this.plugin.id
                return `/log/api/user/logs/${this.projectId}/${this.pipelineId}/${this.buildId}/download?tag=${tag}&executeCount=${this.postData.currentExe}&fileName=${fileName}`
            }
        },

        mounted () {
            this.getLog()
        },

        beforeDestroy () {
            this.closeLog()
        },

        methods: {
            getLog () {
                const id = uuid()
                this.getLog.id = id
                let logMethod = pipelines.getAfterLog
                if (this.postData.lineNo <= 0) logMethod = pipelines.getInitLog

                logMethod(this.postData).then((res) => {
                    if (this.clearIds.includes(id)) return

                    const scroll = this.$refs.scroll
                    res = res.data || {}
                    if (res.status !== 0) {
                        let errMessage
                        switch (res.status) {
                            case 1:
                                errMessage = this.$t('history.logEmpty')
                                break
                            case 2:
                                errMessage = this.$t('history.logClear')
                                break
                            case 3:
                                errMessage = this.$t('history.logClose')
                                break
                            default:
                                errMessage = this.$t('history.logErr')
                                break
                        }
                        scroll.handleApiErr(errMessage)
                        return
                    }

                    const logs = res.logs || []
                    const lastLog = logs[logs.length - 1] || {}
                    const lastLogNo = lastLog.lineNo || this.postData.lineNo - 1 || -1
                    this.postData.lineNo = +lastLogNo + 1

                    const subTags = res.subTags
                    if (subTags && subTags.length > 0) {
                        const tags = subTags.map((tag) => ({ label: tag, value: tag }))
                        tags.unshift({ label: 'ALL', value: '' })
                        scroll.setSubTag(tags)
                    }

                    if (res.finished) {
                        if (res.hasMore) {
                            scroll.addLogData(logs)
                            this.timeId = setTimeout(this.getLog, 100)
                        } else {
                            scroll.addLogData(logs)
                        }
                    } else {
                        scroll.addLogData(logs)
                        this.timeId = setTimeout(this.getLog, 1000)
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                    if (scroll) scroll.handleApiErr(err.message)
                })
            },

            tagChange (val) {
                this.postData.subTag = val
                this.postData.lineNo = 0
                this.closeLog()
                this.getLog()
            },

            changeExecute (execute) {
                this.postData.currentExe = execute
                this.postData.lineNo = 0
                this.closeLog()
                this.getLog()
            },

            closeLog () {
                clearTimeout(this.timeId)
                this.clearIds.push(this.getLog.id)
            },

            handleApiErr (err) {
                const scroll = this.$refs.scroll
                if (scroll) scroll.handleApiErr(err)
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .plugin-log {
        display: flex;
        flex-direction: column;
        flex: 1;
    }

    .log-tools {
        position: absolute;
        right: 20px;
        top: 13px;
        display: flex;
        align-items: center;
        line-height: 30px;
        user-select: none;
        background: none;
    }
</style>
