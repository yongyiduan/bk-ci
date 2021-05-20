<template>
    <section class="stage-home">
        <h3 class="stage-title">
            <span :class="`${stageStatusCls} title-content`">
                <i :class="`bk-icon ${stageStatusIcon}`"></i>
                {{ stage.name }}
            </span>
            <span class="stage-connector" v-if="stageIndex < stageNum - 1">
                <i class="bk-icon icon-right-shape connector-angle"></i>
            </span>
        </h3>

        <job v-for="(job, jobIndex) in stage.containers"
            :job="job"
            :key="job.id"
            :job-index="jobIndex"
            :stage-index="stageIndex"
            :stage-num="stageNum"
        ></job>
    </section>
</template>

<script>
    import job from './job'

    export default {
        components: {
            job
        },

        props: {
            stage: Object,
            stageIndex: Number,
            stageNum: Number
        },

        computed: {
            stageStatusCls () {
                return this.stage && this.stage.status ? this.stage.status : ''
            },

            stageStatusIcon () {
                switch (this.stageStatusCls) {
                    case 'SUCCEED':
                        return 'icon-check-circle'
                    case 'FAILED':
                        return 'icon-close-circle'
                    case 'SKIP':
                        return 'icon-redo-arrow'
                    case 'RUNNING':
                        return 'icon-circle-2-1 spin-icon'
                    default:
                        return ''
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .stage-home {
        position: relative;
        width: 280px;
        border-radius: 2px;
        padding: 0 0 24px 0;
        background: #f5f5f5;
        margin: 0 80px 0 0;
    }
    .stage-title {
        position: relative;
        width: 100%;
        height: 50px;
        line-height: 50px;
        border-radius: 2px;
        text-align: center;
        .title-content {
            width: 100%;
            height: 100%;
            display: block;
            background-color: #f3f3f3;
            border: 1px solid #d0d8ea;
            color: black;
            .bk-icon {
                font-size: 22px;
            }
            &.SKIP {
                color: #c3cdd7;
                fill: #c3cdd7;
            }

            &.RUNNING {
                background-color: #eff5ff;
                border-color: #d4e8ff;
                color: #3c96ff;
            }
            &.REVIEWING {
                background-color: #f3f3f3;
                border-color: #d0d8ea;
                color: black;
            }

            &.FAILED {
                border-color: #ffd4d4;
                background-color: #fff9f9;
                color: black;
                .bk-icon {
                    color: #ff5656;
                }
            }
            &.SUCCEED {
                background-color: #f3fff6;
                border-color: #bbefc9;
                color: black;
                .bk-icon {
                    color: #34da7b;
                }
            }
        }
    }
    .stage-connector {
        position: absolute;
        width: 80px;
        height: 2px;
        right: -81px;
        top: 24px;
        background: #c3cdd7;
        color: #c3cdd7;
        &:before {
            content: '';
            width: 5px;
            height: 10px;
            position: absolute;
            left: -1px;
            top: -4px;
            background-color: #c3cdd7;
            border-radius: 0 100px 100px 0;
        }
        .connector-angle {
            position: absolute;
            right: -2px;
            top: -6px;
        }
    }
</style>
