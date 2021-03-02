<template>
    <span>
        <logo :name="lowerCaseProp()"></logo>{{ getStatusName() }}
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
                (this.status || '').toLowerCase()
            },

            getStatusName () {
                const statusMap = {
                    INIT: '初始化',
                    STAGING: '排队中',
                    STARTING: '准备阶段',
                    RUNNING: '正在加速',
                    FINISH: '成功',
                    FAILED: '失败'
                }
                return statusMap[this.status]
            }
        }
    }
</script>

<style lang="scss" scoped>
    
</style>
