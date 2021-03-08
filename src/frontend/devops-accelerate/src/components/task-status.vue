<template>
    <span class="status-component">
        <logo :name="lowerCaseProp()" :class="`${status} status-logo`"></logo>{{ getStatusName() }}
    </span>
</template>

<script>
    import logo from './logo'

    export default {
        components: {
            logo
        },

        props: {
            status: String
        },

        methods: {
            lowerCaseProp () {
                return (this.status || '').toLowerCase()
            },

            getStatusName () {
                const statusMap = {
                    init: '初始化',
                    staging: '排队中',
                    starting: '准备阶段',
                    running: '正在加速',
                    finish: '成功',
                    failed: '失败'
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
    }
</style>
