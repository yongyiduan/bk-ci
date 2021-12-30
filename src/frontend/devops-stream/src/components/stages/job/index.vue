<template>
    <section ref="pipelineJob" class="job-main">
        <matrix-job v-bind="$props" v-if="job.matrixGroupFlag"></matrix-job>
        <job v-bind="$props" v-else></job>

        <cruve-line v-if="stageIndex > 0" v-bind="cruveLineProp" direction :class="{ 'first-job': jobIndex === 0, 'connect-line left': true }" />
        <cruve-line v-if="stageIndex < stageNum - 1" v-bind="cruveLineProp" :direction="false" :class="{ 'first-job': jobIndex === 0, 'connect-line right': true }" />
    </section>
</template>

<script>
    import job from './job.vue'
    import matrixJob from './matrix-job.vue'
    import cruveLine from './children/cruve-line.vue'

    export default {
        components: {
            job,
            matrixJob,
            cruveLine
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
                cruveLineProp: {},
                observer: null
            }
        },

        mounted () {
            this.initStatus()
        },

        beforeDestroy () {
            this.observer?.disconnect()
        },

        methods: {
            initStatus () {
                this.observer = new MutationObserver(() => {
                    this.initCruveLine()
                })
                this.observer.observe(this.$refs.pipelineJob.previousElementSibling, {
                    attributeFilter: ['style'],
                    subtree: true,
                    attributes: true
                })
                this.initCruveLine()
            },

            initCruveLine () {
                const siblingOffsetHeight = this.$refs.pipelineJob.previousElementSibling.offsetHeight + 15
                const height = this.jobIndex === 0 ? 59 : siblingOffsetHeight
                const style = this.jobIndex === 0 ? 'top: -43px;' : `margin-top: -${siblingOffsetHeight}px;`
                const straight = this.jobIndex !== 0
                const width = this.jobIndex === 0 ? 56 : 50
                this.cruveLineProp = { width, straight, style, height }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .job-main {
        position: relative;
        .first-job {
            &.left {
                left: -39px;
            }
            &.right {
                right: -39px;
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
            left: -30px;
        }
        &.right {
            left: auto;
            right: -30px;
        }
    }
</style>
