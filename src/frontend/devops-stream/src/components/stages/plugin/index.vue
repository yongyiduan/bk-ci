<template>
    <section>
        <section :class="{ 'plugin-item': true, 'first-plugin': pluginIndex === 0, [statusClass]: true }" @click="toggleShowLog">
            <plugin-icon :status-container="plugin" v-if="!isSkip"></plugin-icon>
            <span v-else class="plugin-index">{{ jobIndex + 1 }}-{{ pluginIndex + 1 }}</span>
            <span class="plugin-name text-ellipsis" v-bk-overflow-tips>{{ plugin.name }}</span>
            <span class="plugin-time" v-bk-tooltips="pluginTime" v-if="!isSkip">{{ pluginTime }}</span>
        </section>

        <plugin-log @close="toggleShowLog"
            :log-data="plugin"
            :job-index="jobIndex"
            :plugin-index="pluginIndex"
            :stage-index="stageIndex"
            :matrix-index="matrixIndex"
            v-if="showLog"
        ></plugin-log>
    </section>
</template>

<script>
    import { coverTimer } from '@/utils'
    import { getPipelineStatusClass } from '@/components/status'
    import pluginLog from '@/components/exec-detail/single-log.vue'
    import pluginIcon from '../children/status-icon.vue'

    export default {
        components: {
            pluginLog,
            pluginIcon
        },

        props: {
            plugin: Object,
            pluginIndex: Number,
            jobIndex: Number,
            stageIndex: Number,
            matrixIndex: Number
        },

        data () {
            return {
                showLog: false
            }
        },

        computed: {
            pluginTime () {
                return this.plugin.elapsed > 36e5 ? '1h' : coverTimer(this.plugin.elapsed)
            },

            statusClass () {
                return getPipelineStatusClass(this.plugin.status, this.plugin?.additionalOptions?.enable === false)
            },

            isSkip () {
                return this.statusClass === 'skip'
            }
        },

        methods: {
            toggleShowLog () {
                this.showLog = !this.showLog
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .plugin-item {
        position: relative;
        display: flex;
        flex-direction: row;
        align-items: center;
        width: 100%;
        height: 42px;
        margin-top: 11px;
        background-color: white;
        border-radius: 2px;
        font-size: 14px;
        transition: all .4s ease-in-out;
        border: 1px solid #c3cdd7;
        cursor: pointer;
        .plugin-name {
            flex: 1;
            max-width: 152px;
            color: #7b7d8a;
        }
        .plugin-time {
            font-size: 12px;
            margin-right: 12px;
            margin-left: 5px;
        }
        .plugin-index {
            display: inline-block;
            width: 42px;
            text-align: center;
        }
        &.first-plugin:before {
            top: -16px;
        }
        &:before {
            content: '';
            position: absolute;
            height: 14px;
            width: 2px;
            background: #c3cdd7;
            top: -12px;
            left: 21.5px;
            z-index: 1;
        }
        &:after {
            content: '';
            position: absolute;
            height: 4px;
            width: 4px;
            border: 2px solid #c3cdd7;
            border-radius: 50%;
            background: white;
            top: -5px;
            left: 18.5px;
            z-index: 2;
        }
        &.canceled, &.waiting {
            border-color: $warningColor;
        }
        &.pause {
            border-color: $pauseColor;
        }
        &.warning {
            border-color: $warningColor;
            &:before {
                background: $warningColor;
            }
            &:after {
                border-color: $warningColor;
            }
        }
        &.danger {
            border-color: $dangerColor;
            &:before {
                background: $dangerColor;
            }
            &:after {
                border-color: $dangerColor;
            }
        }
        &.success {
            border-color: $successColor;
            &:before {
                background: $successColor;
            }
            &:after {
                border-color: $successColor;
            }
        }
        &.skip {
            color: #c3cdd7;
            .plugin-name {
                color: #c3cdd7;
                text-decoration: line-through;
            }
        }
    }
</style>
