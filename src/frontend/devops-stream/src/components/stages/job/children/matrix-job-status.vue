<template>
    <span :class="`matrix-job-status ${job.status}`">
        <status-icon :status-container="job" class="status-icon"></status-icon>
    </span>
</template>

<script>
    import statusIcon from '@/components/stages/children/status-icon.vue'

    export default {
        components: {
            statusIcon
        },

        props: {
            job: Object
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .matrix-job-status {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        cursor: pointer;
        .status-icon {
            width: 25px;
        }
        &.RUNNING {
            color: $runningColor;
        }
        &.SUCCESS {
            color: $successColor;
        }
        &.FAILED {
            color: $dangerColor;
        }
        &.SKIP {
            color: $pauseColor;
        }
    }
</style>
