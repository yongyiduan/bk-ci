<template>
    <i :class="iconCls"></i>
</template>

<script>
    export default {
        name: 'status-icons',

        props: {
            status: String,
            type: String
        },

        computed: {
            iconCls () {
                const iconName = 'bk-icon'
                const statusMap = {
                    CANCELED: 'warning',
                    TERMINATE: 'warning',
                    REVIEWING: 'warning',
                    REVIEW_ABORT: 'warning',
                    FAILED: 'danger',
                    HEARTBEAT_TIMEOUT: 'danger',
                    QUEUE_TIMEOUT: 'danger',
                    EXEC_TIMEOUT: 'danger',
                    SUCCEED: 'success'
                }
                const iconMap = {
                    RUNNING: 'icon-circle-2-1 executing-job',
                    PREPARE_ENV: 'icon-circle-2-1 executing-job',
                    QUEUE: 'icon-circle-2-1 executing-job',
                    LOOP_WAITING: 'icon-circle-2-1 executing-job',
                    CALL_WAITING: 'icon-circle-2-1 executing-job',
                    DEPENDENT_WAITING: 'icon-hourglass',
                    WAITING: 'icon-clock',
                    CANCELED: 'icon-exclamation-circle-shape',
                    TERMINATE: 'icon-exclamation-circle-shape',
                    REVIEWING: 'icon-exclamation-triangle-shape',
                    REVIEW_ABORT: 'icon-exclamation-triangle-shape',
                    FAILED: 'icon-close-circle-shape',
                    HEARTBEAT_TIMEOUT: 'icon-close-circle-shape',
                    QUEUE_TIMEOUT: 'icon-close-circle-shape',
                    EXEC_TIMEOUT: 'icon-close-circle-shape',
                    SUCCEED: 'icon-check-circle-shape'
                }
                return [iconName, statusMap[this.status], (iconMap[this.status] || 'CANCELED'), this.type]
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '../../css/conf';

    .executing-job {
        &:before {
            display: inline-block;
            animation: rotating infinite .6s ease-in-out;
        }
    }
    @keyframes rotating {
        from {
            transform: rotate(0)
        }

        to {
            transform: rotate(359deg)
        }
    }
    i {
        width: 42px;
        height: 42px;
        line-height: 42px;
        &.job {
            color: white;
        }
    }
</style>
