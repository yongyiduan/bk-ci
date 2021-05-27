<template>
    <detail-container @close="$emit('close')"
        :title="plugin.name"
        :status="plugin.status"
    >
        <plugin-log :job-index="jobIndex"
            :stage-index="stageIndex"
            :plugin="plugin"
            ref="log"
        />
    </detail-container>
</template>

<script>
    import detailContainer from './detailContainer'
    import pluginLog from './log/pluginLog'

    export default {
        components: {
            detailContainer,
            pluginLog
        },

        props: {
            plugin: Object,
            jobIndex: Number,
            stageIndex: Number
        },

        data () {
            return {
                buildId: this.$route.params.buildId
            }
        }
    }
</script>
