<template>
    <section class="matrix-job-home">
        <span :class="['matrix-head', { 'connect-dot': stageIndex < stages.length - 1 }]" @click="toggleShowLog">
            <icon
                name="angle-down-line"
                size="12"
                :class="{
                    'matrix-head-icon': true,
                    'angle-hidden': !showJobs
                }"
                @click.native.stop="toggleShowJobs"
            ></icon>
            <span :class="['matrix-head-name', 'text-ellipsis', { running: job.status === 'RUNNING' }]" v-bk-overflow-tips @click.stop="showJobs = !showJobs">{{ job.name }}</span>
            <matrix-job-status :job="job"></matrix-job-status>
            <i class="bk-icon icon-right-shape connector-angle" v-if="stageIndex !== 0"></i>
        </span>

        <bk-transition name="collapse" duration-time="200ms">
            <section v-show="showJobs" class="matrix-job-body">
                <section
                    v-for="(groupContainer, index) in job.groupContainers || []"
                    :key="index"
                    class="matrix-job-single">
                    <job-home v-bind="$props" :job="getMatrixJob(groupContainer)" is-matrix></job-home>
                    <icon
                        name="angle-down-line"
                        size="16"
                        :class="{
                            [getPipelineStatusClass(groupContainer.status, isSkip(groupContainer))]: true,
                            'plugin-show-icon': true,
                            'plugin-hidden': !showPluginsJobIds.includes(groupContainer.containerHashId)
                        }"
                        @click.native="togglePluginShow(groupContainer)"
                    ></icon>
                    <bk-transition name="collapse" duration-time="200ms">
                        <section v-show="showPluginsJobIds.includes(groupContainer.containerHashId)" class="matrix-plugin-list">
                            <plugin-list
                                v-bind="$props"
                                :matrix-index="index"
                                :job="getPluginContainer(groupContainer)"
                            ></plugin-list>
                        </section>
                    </bk-transition>
                </section>
            </section>
        </bk-transition>

        <single-log @close="toggleShowLog"
            :log-data="{ ...job, name: 'Matrix Job' }"
            :job-index="jobIndex"
            :stage-index="stageIndex"
            v-if="showLog"
        ></single-log>
    </section>
</template>

<script>
    import jobHome from './children/job-home.vue'
    import pluginList from './children/plugin-list.vue'
    import matrixJobStatus from './children/matrix-job-status.vue'
    import singleLog from '@/components/exec-detail/single-log.vue'
    import { getPipelineStatusClass } from '@/components/status'

    export default {
        components: {
            jobHome,
            pluginList,
            matrixJobStatus,
            singleLog
        },

        props: {
            job: Object,
            jobIndex: Number,
            stages: Array,
            stageIndex: Number,
            stageNum: Number
        },

        data () {
            return {
                showJobs: true,
                showPluginsJobIds: [],
                showLog: false
            }
        },

        methods: {
            getPipelineStatusClass,

            isSkip (groupContainer) {
                return groupContainer?.jobControlOption?.enable === false
            },

            toggleShowJobs () {
                this.showJobs = !this.showJobs
            },

            togglePluginShow (groupContainer) {
                const index = this.showPluginsJobIds.findIndex(hidePluginsJobId => groupContainer.containerHashId === hidePluginsJobId)
                if (index > -1) {
                    this.showPluginsJobIds.splice(index, 1)
                } else {
                    this.showPluginsJobIds.push(groupContainer.containerHashId)
                }
            },

            toggleShowLog () {
                this.showLog = !this.showLog
            },

            getMatrixJob (groupContainer = {}) {
                const env = Object.values(groupContainer.matrixContext || {}).join(',')
                const envStr = env ? `（${env}）` : ''
                return {
                    ...groupContainer,
                    name: groupContainer.name + envStr
                }
            },

            getPluginContainer (groupContainer) {
                groupContainer.elements?.forEach((element, index) => {
                    const elementModel = this.job?.elements?.[index]
                    element.additionalOptions = elementModel.additionalOptions
                })
                return groupContainer
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .matrix-job-home {
        width: 240px;
        background: #FFFFFF;
        border: 1px solid #B5C0D5;
        border-radius: 2px;
        padding: 0 12px;
        margin-top: 16px;
    }
    .matrix-head {
        display: flex;
        align-items: center;
        position: relative;
        .matrix-head-icon {
            cursor: pointer;
            transition: transform 200ms;
            transform: rotate(-180deg);
            &.angle-hidden {
                transform: rotate(0);
            }
        }
        .matrix-head-name {
            cursor: pointer;
            max-width: 120px;
            font-size: 14px;
            margin: 0 9px;
            &.running {
                max-width: 50px;
            }
        }
        .connector-angle {
            position: absolute;
            color: #c3cdd7;
            left: -24px;
            top: 14px;
            font-size: 12px;
        }
        &.connect-dot:before {
            content: '';
            width: 3px;
            height: 6px;
            position: absolute;
            right: -16px;
            top: 17px;
            background-color: #c3cdd7;
            border-radius: 0 100px 100px 0;
        }
    }
    .matrix-job-body {
        margin-bottom: 16px;
        .matrix-job-single {
            position: relative;
            margin-bottom: 16px;
        }
        .matrix-plugin-list {
            padding-top: 5px;
        }
        .plugin-show-icon {
            position: absolute;
            padding: 3px;
            border: 1px solid;
            border-radius: 100px;
            z-index: 10;
            left: 99px;
            top: 35px;
            cursor: pointer;
            background: #fff;
            transition: transform 200ms;
            transform: rotate(-180deg);
            &.plugin-hidden {
                transform: rotate(0);
            }
            &.running {
                color: #459fff;
                border-color: #459fff;
            }
            &.canceled {
                color: #f6b026;
                border-color: #f6b026;
            }
            &.danger {
                color: #ff5656;
                border-color: #ff5656;
            }
            &.success {
                color: #34d97b;
                border-color: #34d97b;
            }
            &.pause {
                color: #ff9801;
                border-color: #ff9801;
            }
            &.skip {
                color: #c3cdd7;
                border-color: #c3cdd7;
            }
        }
    }
</style>
