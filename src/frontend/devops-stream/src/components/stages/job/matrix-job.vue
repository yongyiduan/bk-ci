<template>
    <section class="matrix-job-home">
        <span class="matrix-head">
            <icon
                name="angle-down-line"
                size="12"
                :class="{
                    'matrix-head-icon': true,
                    'angle-hidden': !showJobs
                }"
                @click.native="showJobs = !showJobs"
            ></icon>
            <span class="matrix-head-name text-ellipsis" v-bk-overflow-tips @click="showJobs = !showJobs">{{ job.name }}</span>
            <matrix-job-status :job="job" @click.native="toggleShowLog"></matrix-job-status>
        </span>

        <bk-transition name="collapse" duration-time="200ms">
            <section v-show="showJobs" class="matrix-job-body">
                <section
                    v-for="(groupContainer, index) in job.groupContainers || []"
                    :key="index"
                    class="matrix-job-single">
                    <job-home v-bind="$props" :job="groupContainer"></job-home>
                    <icon
                        name="angle-down-line"
                        size="16"
                        :class="{
                            [getPipelineStatusClass(groupContainer.status)]: true,
                            'plugin-show-icon': true,
                            'plugin-hidden': hidePluginsJobIds.includes(groupContainer.containerHashId)
                        }"
                        @click.native="togglePluginShow(groupContainer)"
                    ></icon>
                    <bk-transition name="collapse" duration-time="200ms">
                        <section v-show="!hidePluginsJobIds.includes(groupContainer.containerHashId)" class="matrix-plugin-list">
                            <plugin-list
                                v-bind="$props"
                                :matrix-index="index"
                                :job="groupContainer"
                            ></plugin-list>
                        </section>
                    </bk-transition>
                </section>
            </section>
        </bk-transition>

        <single-log @close="toggleShowLog"
            :log-data="job"
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
                showJobs: false,
                hidePluginsJobIds: [],
                showLog: false
            }
        },

        methods: {
            getPipelineStatusClass,

            togglePluginShow (groupContainer) {
                const index = this.hidePluginsJobIds.findIndex(hidePluginsJobId => groupContainer.containerHashId === hidePluginsJobId)
                if (index > -1) {
                    this.hidePluginsJobIds.splice(index, 1)
                } else {
                    this.hidePluginsJobIds.push(groupContainer.containerHashId)
                }
            },

            toggleShowLog () {
                this.showLog = !this.showLog
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
        .matrix-head-icon {
            cursor: pointer;
            transition: transform 200ms;
            &.angle-hidden {
                transform: rotate(-180deg);
            }
        }
        .matrix-head-name {
            cursor: pointer;
            flex: 1;
            font-size: 14px;
            margin: 0 9px;
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
            &.plugin-hidden {
                transform: rotate(-180deg);
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
