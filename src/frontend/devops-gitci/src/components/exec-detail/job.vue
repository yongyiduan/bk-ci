<template>
    <detail-container @close="$emit('close')"
        :title="job.name"
        :status="job.status"
    >
        <span
            v-if="job.dispatchType && job.dispatchType.buildType === 'GIT_CI'"
            class="web-console"
            :style="{ right: executeCount > 1 ? '390px' : '280px' }"
            @click="startDebug"
        >
            Web Console
        </span>
        <job-log :plugin-list="pluginList"
            :build-id="$route.params.buildId"
            :down-load-link="downLoadJobLink"
            :execute-count="executeCount"
            ref="jobLog"
        />
    </detail-container>
</template>

<script>
    import { mapState } from 'vuex'
    import jobLog from './log/jobLog'
    import detailContainer from './detailContainer'
    import { pipelines } from '@/http'

    export default {
        components: {
            detailContainer,
            jobLog
        },

        props: {
            job: Object,
            stages: Object,
            stageIndex: Number,
            jobIndex: Number
        },

        computed: {
            ...mapState(['projectId']),
            
            pipelineId () {
                return this.$route.params.pipelineId || ''
            },

            buildId () {
                return this.$route.params.buildId || ''
            },

            hashId () {
                return this.$route.hash
            },

            downLoadJobLink () {
                const fileName = encodeURI(encodeURI(`${this.stageIndex + 1}-${this.jobIndex + 1}-${this.job.name}`))
                const jobId = this.job.containerId
                const { pipelineId, buildId } = this.$route.params
                return `/log/api/user/logs/${this.projectId}/${pipelineId}/${buildId}/download?jobId=${jobId}&fileName=${fileName}`
            },

            pluginList () {
                const startUp = { name: 'Set up job', status: this.job.startVMStatus, id: `startVM-${this.job.id}`, executeCount: this.job.executeCount || 1 }
                return [startUp, ...this.job.elements]
            },

            executeCount () {
                const executeCountList = this.pluginList.map((plugin) => plugin.executeCount || 1)
                return Math.max(...executeCountList)
            }
        },
        methods: {
            startDebug () {
                const vmSeqId = this.getRealSeqId()
                if (this.job.status === 'RUNNING') {
                    this.getContainerInfoById(vmSeqId)
                } else {
                    this.startNewDocker(vmSeqId)
                }
            },

            getContainerInfoById (vmSeqId) {
                let url = ''
                const tab = window.open('about:blank')
                pipelines.getContainerInfoByBuildId(this.projectId, this.pipelineId, this.buildId, vmSeqId).then((res) => {
                    if (res.containerId && res.address) {
                        url = `/webConsole/?pipelineId=${this.pipelineId}&buildId=${this.buildId}&containerId=${res.containerId}&targetIp=${res.address}${this.hashId}`
                    } else {
                        tab.close()
                    }
                }).catch((err) => {
                    tab.close()
                    this.$bkMessage({
                        theme: 'error',
                        message: err.message || err
                    })
                }).finally(() => {
                    tab.location = url
                })
            },

            startNewDocker (vmSeqId) {
                let url = ''
                const tab = window.open('about:blank')
                pipelines.startDebugDocker(
                    {
                        projectId: this.projectId,
                        pipelineId: this.pipelineId,
                        vmSeqId,
                        containerPool: this.job.dispatchType.value
                    }).then((res) => {
                    if (res === true) {
                        url = `/webConsole/?pipelineId=${this.pipelineId}&buildId=${this.buildId}&vmSeqId=${vmSeqId}${this.hashId}`
                    } else {
                        tab.close()
                    }
                })
                    .catch((err) => {
                        tab.close()
                        this.$bkMessage({
                            theme: 'error',
                            message: err.message || err
                        })
                    }).finally(() => {
                        tab.location = url
                    })
            },

            getRealSeqId () {
                return this.stages.slice(0, this.stageIndex).reduce((acc, stage) => {
                    acc += stage.containers.length
                    return acc
                }, 0) + this.jobIndex + 1
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .web-console {
        position: absolute;
        right: 280px;
        top: 20px;
        color: #3c96ff;
        cursor: pointer;
    }
</style>
