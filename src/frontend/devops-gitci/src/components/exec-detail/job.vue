<template>
    <detail-container @close="$emit('close')"
        :title="job.name"
        :status="job.status"
    >
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

    export default {
        components: {
            detailContainer,
            jobLog
        },

        props: {
            job: Object,
            stageIndex: Number,
            jobIndex: Number
        },

        computed: {
            ...mapState(['projectId']),

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
        }
    }
</script>
