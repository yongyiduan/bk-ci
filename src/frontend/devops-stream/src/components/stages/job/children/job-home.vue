<template>
    <section class="job-home">
        <h3 :class="{ 'job-title': true, 'connect-dot': (stageIndex < stageNum - 1 && !isMatrix), [statusClass]: true }" @click="toggleShowLog">
            <i :class="[jobStatusIcon, 'job-status']" v-if="statusClass && !isSkip"></i>
            <span v-else class="job-index">{{ stageIndex + 1 }}-{{ jobIndex + 1 }}</span>
            <span class="job-name text-ellipsis" v-bk-overflow-tips>{{ job.status === 'PREPARE_ENV' && !isMatrix ? '准备构建环境中' : job.name }}</span>
            <job-time :job="job"></job-time>
            <i class="bk-icon icon-right-shape connector-angle" v-if="stageIndex !== 0 && !isMatrix"></i>
        </h3>

        <job-log :job="job" v-if="showJobLog" :job-index="jobIndex" :stages="stages" :stage-index="stageIndex" @close="toggleShowLog"></job-log>
    </section>
</template>

<script>
    import { getPipelineStatusClass, getPipelineStatusShapeIconCls } from '@/components/status'
    import jobTime from './job-time'
    import jobLog from '@/components/exec-detail/job'

    export default {
        components: {
            jobTime,
            jobLog
        },

        props: {
            job: Object,
            jobIndex: Number,
            stages: Array,
            stageIndex: Number,
            stageNum: Number,
            isMatrix: Boolean
        },

        data () {
            return {
                showJobLog: false
            }
        },

        computed: {
            jobStatusIcon () {
                return getPipelineStatusShapeIconCls(this.job.status || 'WAITING')
            },

            statusClass () {
                return getPipelineStatusClass(this.job.status, this.job?.jobControlOption?.enable === false)
            },

            isSkip () {
                return this.statusClass === 'skip'
            }
        },

        methods: {
            toggleShowLog () {
                this.showJobLog = !this.showJobLog
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .job-home {
        position: relative;
        .job-title {
            position: relative;
            width: 100%;
            z-index: 3;
            color: #fff;
            height: 42px;
            display: flex;
            align-items: center;
            font-weight: 600;
            background-color: #63656e;
            cursor: pointer;
            .job-status {
                width: 42px;
                height: 42px;
                line-height: 42px;
                color: #fff;
            }
            .job-index {
                display: inline-block;
                width: 42px;
                text-align: center;
                font-weight: normal;
            }
            &.running {
                background-color: #459fff;
            }
            &.canceled {
                background-color: #f6b026;
            }
            &.danger {
                background-color: #ff5656;
            }
            &.success {
                background-color: #34d97b;
            }
            &.pause {
                background-color: #ff9801;
            }
            &.skip {
                color: #c3cdd7;
                .job-name {
                    color: #c3cdd7;
                    text-decoration: line-through;
                }
            }

            .job-name {
                flex: 1;
                max-width: 152px;
            }
            .connector-angle {
                position: absolute;
                color: #c3cdd7;
                left: -11px;
                top: 15px;
                font-size: 12px;
            }
            &.connect-dot:before {
                content: '';
                width: 3px;
                height: 6px;
                position: absolute;
                right: -3px;
                top: 18px;
                background-color: #c3cdd7;
                border-radius: 0 100px 100px 0;
            }
        }
    }
</style>
