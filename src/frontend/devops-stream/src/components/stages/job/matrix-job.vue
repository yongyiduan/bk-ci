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
                @click.native="showJobs=!showJobs"
            ></icon>
            <span class="matrix-head-name text-ellipsis" v-bk-overflow-tips>{{ job.name }}</span>
            <span class="matrix-head-percent">
                <icon name="loading" size="12"></icon>
                <span class="matrix-head-percent-main">进度{{ getPercent() }}</span>
                （{{ job.matrixControlOption.runningCount }}/{{ job.matrixControlOption.totalCount }}）
            </span>
        </span>

        <bk-transition name="collapse" duration-time="200ms">
            <section v-show="showJobs" class="matrix-job-body">
                <template v-for="(groupContainer, index) in job.groupContainers || []">
                    <job-home v-bind="$props" :job="groupContainer" :key="index"></job-home>
                    <icon
                        :key="index"
                        name="angle-down-line"
                        size="16"
                        :class="{
                            [statusClass]: true,
                            'plugin-show-icon': true,
                            'plugin-hidden': hidePluginsJobIds.includes(groupContainer.jobId)
                        }"
                        @click.native="togglePluginShow(groupContainer)"
                    ></icon>
                    <bk-transition name="collapse" duration-time="200ms" :key="index">
                        <section v-show="!hidePluginsJobIds.includes(groupContainer.jobId)" class="matrix-plugin-list">
                            <plugin-list
                                v-bind="$props"
                                :job="groupContainer"
                            ></plugin-list>
                        </section>
                    </bk-transition>
                </template>
            </section>
        </bk-transition>
    </section>
</template>

<script>
    import jobHome from './children/job-home.vue'
    import pluginList from './children/plugin-list.vue'
    import { getPipelineStatusClass } from '@/components/status'

    export default {
        components: {
            jobHome,
            pluginList
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
                hidePluginsJobIds: []
            }
        },

        computed: {
            statusClass () {
                return getPipelineStatusClass(this.job.status)
            }
        },

        methods: {
            getPercent () {
                const value = this.job.matrixControlOption.runningCount * 100 / (this.job.matrixControlOption.totalCount || 1)
                return Number(value.toString().match(/^\d+(?:\.\d{0,2})?/)) + '%'
            },

            togglePluginShow (groupContainer) {
                const index = this.hidePluginsJobIds.findIndex(hidePluginsJobId => groupContainer.jobId === hidePluginsJobId)
                if (index > -1) {
                    this.hidePluginsJobIds.splice(index, 1)
                } else {
                    this.hidePluginsJobIds.push(groupContainer.jobId)
                }
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
        margin: 11px 0;
        .matrix-head-icon {
            cursor: pointer;
            transition: transform 200ms;
            &.angle-hidden {
                transform: rotate(-180deg);
            }
        }
        .matrix-head-name {
            flex: 1;
            font-size: 14px;
            margin: 0 9px;
        }
        .matrix-head-percent {
            font-size: 12px;
            display: flex;
            align-items: center;
            color: #3480FF;
            .matrix-head-percent-main {
                margin-left: 3px;
            }
        }
    }
    .matrix-job-body {
        position: relative;
        margin-bottom: 16px;
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
