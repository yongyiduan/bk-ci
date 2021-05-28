<template>
    <section class="job-home" ref="pipelineJob">
        <h3 :class="{ 'job-title': true, 'connect-dot': stageIndex < stageNum - 1, [getPipelineStatusClass(job.status)]: true }" @click="toggleShowLog">
            <i :class="[jobStatusIcon, 'job-status']"></i>
            <span class="job-name">{{ job.status === 'PREPARE_ENV' ? '准备构建环境中' : job.name }}</span>
            <job-time :job="job"></job-time>
            <i class="bk-icon icon-right-shape connector-angle" v-if="stageIndex !== 0"></i>
        </h3>

        <cruve-line v-if="stageIndex > 0" v-bind="cruveLineProp" direction :class="{ 'first-job': jobIndex === 0, 'connect-line left': true }" />
        <cruve-line v-if="stageIndex < stageNum - 1" v-bind="cruveLineProp" :direction="false" :class="{ 'first-job': jobIndex === 0, 'connect-line right': true }" />

        <plugin v-for="(plugin, pluginIndex) in job.elements"
            :plugin="plugin"
            :key="plugin.id"
            :plugin-index="pluginIndex"
            :job-index="jobIndex"
            :stage-index="stageIndex"
        ></plugin>

        <job-log :job="job" v-if="showJobLog" :job-index="jobIndex" :stage-index="stageIndex" @close="toggleShowLog"></job-log>
    </section>
</template>

<script>
    import { getPipelineStatusClass, getPipelineStatusShapeIconCls } from '@/components/status'
    import cruveLine from './cruve-line'
    import plugin from '../plugin/index'
    import jobTime from './job-time'
    import jobLog from '@/components/exec-detail/job'

    export default {
        components: {
            cruveLine,
            plugin,
            jobTime,
            jobLog
        },

        props: {
            job: Object,
            jobIndex: Number,
            stageIndex: Number,
            stageNum: Number
        },

        data () {
            return {
                cruveLineProp: {},
                showJobLog: false
            }
        },

        computed: {
            jobStatusIcon () {
                return getPipelineStatusShapeIconCls(this.job.status || 'WAITING')
            }
        },

        mounted () {
            this.initStatus()
        },

        methods: {
            getPipelineStatusClass,

            initStatus () {
                const siblingOffsetHeight = this.$refs.pipelineJob.previousElementSibling.offsetHeight + 15
                const height = this.jobIndex === 0 ? 59 : siblingOffsetHeight
                const style = this.jobIndex === 0 ? 'top: -43px;' : `margin-top: -${siblingOffsetHeight}px;`
                const straight = this.jobIndex !== 0
                const width = this.jobIndex === 0 ? 56 : 50
                this.cruveLineProp = { width, straight, style, height }
            },

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
        margin: 16px 20px 0 20px;
        .job-title {
            position: relative;
            margin: 0 0 16px 0;
            width: 240px;
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

            .job-name {
                flex: 1;
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
        .first-job {
            &.left {
                left: -59px;
            }
            &.right {
                right: -59px;
            }
        }
    }
    .connect-line {
        position: absolute;
        top: 16px;
        stroke: #c3cdd7;
        stroke-width: 1;
        fill: none;
        z-index: 0;
        &.left {
            left: -50px;
        }
        &.right {
            left: auto;
            right: -50px;
        }
    }
</style>
