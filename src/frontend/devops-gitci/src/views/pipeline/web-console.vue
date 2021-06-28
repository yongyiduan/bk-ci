<template>
    <section>
        <section v-if="!connectError" style="height: 100%" v-bkloading="{
            isLoading,
            title: loadingTitle
        }">
            <div class="console-header">
                <bk-button class="debug-btn" theme="danger" @click="stopDebug">Stop Debug</bk-button>
                <p class="debug-tips" v-show="isRunning">{{ fromRunningTips }}</p>
            </div>
            <div class="container">
                <my-terminal v-if="!isLoading" :url="url" :resize-url="resizeUrl" :exec-id="execId"></my-terminal>
            </div>
            <div class="footer"></div>
        </section>
        <empty-tips
            v-else
            :title="config.title"
            :desc="config.desc"
            :btns="config.btns">
        </empty-tips>
    </section>
</template>

<script>
    import { mapState } from 'vuex'
    import { pipelines } from '@/http'
    import Console from '@/components/Xterm/Console'
    import emptyTips from '@/components/empty-tips'

    export default {
        name: 'WebSSH',
        components: {
            'my-terminal': Console,
            emptyTips
        },
        data () {
            return {
                connectError: false,
                timer: null,
                url: '',
                resizeUrl: '',
                execId: '',
                isRunning: false,
                isExiting: false,
                config: {
                    title: 'Failed in login to debug',
                    desc: 'Illegal name for mirroring or other errors'
                },
                containerName: '',
                fromRunningTips: 'Go to the current debugging through the portal of pipeline at runtime and the container for debugging is the one used for the current build'
            }
        },
        computed: {
            ...mapState(['projectId']),
            isLoading () {
                return !this.url
            },
            loadingTitle () {
                return !this.isExiting ? `If the container can't be activated after one minute you did so, please contact DevOps (customer service from Blue Shield)` : 'exiting'
            },
            consoleType () {
                return this.$route.query.type || 'DOCKER'
            },
            pipelineId () {
                return this.$route.query.pipelineId
            },
            buildId () {
                return this.$route.query.buildId
            },
            targetIp () {
                return this.$route.query.targetIp
            },
            vmSeqId () {
                return this.$route.query.vmSeqId
            },
            containerId () {
                return this.$route.query.containerId
            }
        },
        async created () {
            if (this.consoleType === 'DOCKER') {
                if (this.targetIp && this.pipelineId && this.containerId) {
                    this.isRunning = true
                    this.getLinkDetail(this.containerId, this.targetIp)
                } else {
                    await this.getContainerInfo()
                }
            }
        },
        mounted () {
            this.addLeaveListenr()
        },
        beforeDestroy () {
            this.removeLeaveListenr()
        },
        methods: {
            async getContainerInfo () {
                clearTimeout(this.timer)
                try {
                    const res = await pipelines.getDebugStatus(this.projectId, this.pipelineId, this.vmSeqId)
                    if (res && res.status === 2 && res.containerId && res.address) {
                        // this.url = `ws://${PROXY_URL_PREFIX}/docker-console?pipelineId=${this.pipelineId}&containerId=${res.containerId}&projectId=${this.projectId}&targetIP=${res.address}`
                        this.getLinkDetail(res.containerId, res.address)
                    } else {
                        this.timer = setTimeout(async () => {
                            await this.getContainerInfo()
                        }, 5000)
                    }
                } catch (err) {
                    if (err && err.code === 1) {
                        this.connectError = true
                        this.config.desc = err.message || 'Illegal name for mirroring or other errors'
                    } else {
                        this.$bkMessage({
                            theme: 'error',
                            message: err.message || err
                        })
                    }
                }
            },
            async stopDebug () {
                const content = 'You will leave this page after stop debugging sucessfully'

                this.$bkInfo({
                    title: 'Confirm stopping debugging',
                    subTitle: content,
                    confirmLoading: true,
                    confirmFn: async () => {
                        try {
                            if (this.consoleType === 'DOCKER') {
                                await pipelines.stopDebugDocker(this.projectId, this.pipelineId, this.vmSeqId)
                            }
                            this.$router.push({
                                name: 'pipelineDetail',
                                params: {
                                    pipelineId: this.pipelineId,
                                    buildId: this.buildId
                                }
                            })
                            return true
                        } catch (err) {
                            this.isExiting = false
                            this.$bkMessage({
                                theme: 'error',
                                message: err.message || err
                            })
                            return false
                        }
                    }
                })
            },
            async getLinkDetail (containerId, targetIp) {
                try {
                    if (!containerId || !targetIp) {
                        throw Error('Abnormal parameters')
                    }
                    const cmd = ['/bin/bash']
                    const execId = await pipelines.getDockerExecId(containerId, this.projectId, this.pipelineId, cmd, targetIp)
                    this.execId = execId
                    this.resizeUrl = `docker-console-resize?pipelineId=${this.pipelineId}&projectId=${this.projectId}&targetIp=${targetIp}`
                    const protocol = document.location.protocol === 'https:' ? 'wss:' : 'ws:'
                    this.url = `${protocol}//${PROXY_URL_PREFIX}/docker-console-new?eventId=${execId}&pipelineId=${this.pipelineId}&projectId=${this.projectId}&targetIP=${targetIp}&containerId=${containerId}`
                } catch (err) {
                    this.$bkMessage({
                        message: err.message,
                        theme: 'error'
                    })
                }
            },
            addLeaveListenr () {
                window.addEventListener('beforeunload', this.leaveSure)
            },
            removeLeaveListenr () {
                window.removeEventListener('beforeunload', this.leaveSure)
            },
            leaveSure (e) {
                const dialogText = 'If leave, the data of new edition will be lost'
                e.returnValue = dialogText
                return dialogText
            }
        }
    }
</script>

<style lang='postcss'>
    @import '@/css/conf';

    .console-header {
        height: 60px;
        line-height: 60px;
        background-color: #000;
        .debug-btn {
            float: right;
            margin: 12px 20px;
        }
        .debug-tips {
            float: right;
            font-size: 14px;
        }
    }
    .container {
        height: calc(100% - 160px);
    }
    .footer {
        height: 100px;
        background-color: #000;
    }
</style>
