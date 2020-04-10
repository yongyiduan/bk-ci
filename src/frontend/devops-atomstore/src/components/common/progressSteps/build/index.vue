<template>
    <section class="main-body">
        <ul class="progress-log" v-bkloading="{ isLoading: isLogLoading }">
            <li class="log-item" v-for="(log, index) in logs" :key="index">
                [{{index + 1}}] - {{log.time|timeFilter}}
                <span :style="`color: ${log.color}`"> {{log.message}}</span>
            </li>
        </ul>

        <footer class="main-footer">
            <bk-button :disabled="currentStep.status !== 'success'" theme="primary" @click="nextStep"> {{ $t('store.下一步') }} </bk-button>
            <bk-button :disabled="currentStep.status !== 'fail'" :loading="isLoading" @click="rebuild"> {{ $t('store.重新构建') }} </bk-button>
        </footer>
    </section>
</template>

<script>
    import ansiParse from './ansiParse.js'

    function prezero (num) {
        num = Number(num)
        if (num < 10) return '0' + num
        return num
    }

    function millisecond (num) {
        num = Number(num)
        if (num < 10) return '00' + num
        else if (num < 100) return '0' + num
        return num
    }

    export default {
        filters: {
            timeFilter (val) {
                if (!val) return ''
                const time = new Date(val)
                return `${time.getFullYear()}-${prezero(time.getMonth() + 1)}-${prezero(time.getDate())} ${prezero(time.getHours())}:${prezero(time.getMinutes())}:${prezero(time.getSeconds())}:${millisecond(time.getMilliseconds())}`
            }
        },

        props: {
            currentStep: {
                type: Object
            },
            detail: {
                type: Object
            },
            storeBuildInfo: {
                type: Object
            }
        },

        data () {
            return {
                cancelIds: [],
                logs: [],
                isLoading: false,
                isLogLoading: false
            }
        },

        mounted () {
            this.getLog()
        },

        beforeDestroy () {
            clearTimeout(this.getLog.id)
            this.cancelIds.push(this.getLog.id)
        },

        methods: {
            getLog (id) {
                if (this.getLog.start === undefined) this.isLogLoading = true
                const postData = {
                    type: 'SERVICE',
                    projectCode: this.storeBuildInfo.projectCode,
                    pipelineId: this.storeBuildInfo.pipelineId,
                    buildId: this.storeBuildInfo.buildId,
                    start: this.getLog.start > 0 ? this.getLog.start + 1 : 0,
                    executeCount: this.getLog.executeCount || (this.getLog.executeCount = 1, 1)
                }
                this.$store.dispatch('store/requestProgressLog', postData).then((data = {}) => {
                    if (id && this.cancelIds.includes(id)) return
                    this.isLogLoading = false
                    if (data.status === 0) {
                        const logs = data.logs || []
                        const lastLog = logs[logs.length - 1] || {}
                        this.getLog.start = lastLog.lineNo || this.getLog.start || 0
                        logs.forEach((log) => {
                            const val = log.message
                            const parseRes = ansiParse(val) || [{ message: '', hasHandle: false }]
                            const res = { message: '', time: log.timestamp }
                            parseRes.forEach((item) => {
                                res.message += item.message
                                if (!res.color && item.color) res.color = item.color
                            })
                            this.logs.push(res)
                        })
                        if (!data.finished || data.hasMore) this.getLog.id = setTimeout(() => this.getLog(this.getLog.id), 300)
                    }
                }).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => {
                    const ele = document.querySelector('.progress-log')
                    ele.scrollTop = ele.scrollHeight
                })
            },

            rebuild () {
                clearTimeout(this.getLog.id)
                this.cancelIds.push(this.getLog.id)
                this.isLoading = true
                const postData = {
                    id: this.detail.serviceId,
                    projectCode: this.detail.projectCode
                }
                this.$store.dispatch('store/requestRebuildService', postData).then(() => {
                    this.logs = []
                    this.getLog.start = 0
                    this.$emit('loopProgress', this.getLog)
                }).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            nextStep () {
                this.$parent.currentStepIndex++
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .progress-log {
        background: #f5f5f5;
        border: 1px solid #d9d9d9;
        height: 100%;
        width: 100%;
        overflow: auto;
        padding: 16px;
        .log-item {
            font-family: Consolas, "Courier New", monospace;
            width: 100%;
            word-break: break-all;
            font-size: 12px;
            line-height: 22px;
            color: $grayBlack;
            &:hover {
                background: #3330303d;
            }
        }
    }
</style>
