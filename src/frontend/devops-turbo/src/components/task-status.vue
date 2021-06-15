<template>
    <span class="status-component">
        <logo size="14" :name="lowerCaseProp()" :class="`${status} status-logo`"></logo><span :class="{ error: message }" v-bk-tooltips="{ content: message, disabled: !message }">{{ getStatusName() }}</span>
    </span>
</template>

<script>
    import logo from './logo'

    export default {
        components: {
            logo
        },

        props: {
            status: String,
            message: String
        },

        methods: {
            lowerCaseProp () {
                return (this.status || '').toLowerCase()
            },

            getStatusName () {
                const statusMap = {
                    init: this.$t('turbo.初始化'),
                    staging: this.$t('turbo.排队中'),
                    starting: this.$t('turbo.准备阶段'),
                    running: this.$t('turbo.正在加速'),
                    finish: this.$t('turbo.成功'),
                    failed: this.$t('turbo.失败')
                }
                return statusMap[this.status]
            }
        }
    }
</script>

<style lang="scss" scoped>
    .status-component {
        display: flex;
        align-items: center;
        .status-logo {
            margin-right: 6px;
        }
        .failed {
            color: #fd9c9c;
        }
        .finish {
            color: #86e7a9;
        }
        .error {
            text-decoration: underline dotted #fd9c9c;
        }
    }
</style>
