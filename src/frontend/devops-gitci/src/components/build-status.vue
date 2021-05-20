<template>
    <i :class="iconCls"></i>
</template>

<script>
    export default {
        name: 'build-status',

        props: {
            status: String
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
                    RUNNING: 'icon-circle-2-1 executing-icon',
                    PREPARE_ENV: 'icon-circle-2-1 executing-icon',
                    QUEUE: 'icon-circle-2-1 executing-icon',
                    LOOP_WAITING: 'icon-circle-2-1 executing-icon',
                    CALL_WAITING: 'icon-circle-2-1 executing-icon',
                    DEPENDENT_WAITING: 'icon-hourglass',
                    WAITING: 'icon-clock',
                    CANCELED: 'icon-exclamation',
                    TERMINATE: 'icon-exclamation',
                    REVIEWING: 'icon-exclamation-triangle',
                    REVIEW_ABORT: 'icon-exclamation-triangle',
                    FAILED: 'icon-close',
                    HEARTBEAT_TIMEOUT: 'icon-close',
                    QUEUE_TIMEOUT: 'icon-close',
                    EXEC_TIMEOUT: 'icon-close',
                    SUCCEED: 'icon-check-1'
                }
                return [iconName, statusMap[this.status], iconMap[this.status], this.type]
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .executing-icon {
        font-size: 14px !important;
        &:before {
            display: inline-block;
            animation: rotating infinite .6s ease-in-out;
        }
    }
    .icon-exclamation, .icon-exclamation-triangle, .icon-clock {
        font-size: 24px !important;
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
        width: 32px;
        height: 32px;
        line-height: 32px;
    }
</style>
