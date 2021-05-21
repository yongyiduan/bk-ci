<template>
    <section>
        <section :class="{ 'plugin-item': true, 'first-plugin': pluginIndex === 0, [getPipelineStatusClass(plugin.status)]: true }" @click="toggleShowLog">
            <status-icon :status="plugin.status"></status-icon>
            <span class="plugin-name text-ellipsis" v-bk-overflow-tips>{{ plugin.name }}</span>
            <span class="plugin-time" v-bk-tooltips="pluginTime">{{ pluginTime }}</span>
        </section>

        <plugin-log @close="toggleShowLog"
            :plugin="plugin"
            :job-index="jobIndex"
            :plugin-index="pluginIndex"
            :stage-index="stageIndex"
            v-if="showLog"
        ></plugin-log>
    </section>
</template>

<script>
    import { coverTimer, getPipelineStatusClass } from '@/utils'
    import statusIcon from './status-icon'
    import pluginLog from '../exec-detail/plugin'

    export default {
        components: {
            statusIcon,
            pluginLog
        },

        props: {
            plugin: Object,
            pluginIndex: Number,
            jobIndex: Number,
            stageIndex: Number
        },

        data () {
            return {
                showLog: false
            }
        },

        computed: {
            pluginTime () {
                return this.plugin.elapsed > 36e5 ? '1h' : coverTimer(this.plugin.elapsed)
            }
        },

        methods: {
            getPipelineStatusClass,

            toggleShowLog () {
                this.showLog = !this.showLog
            }
        }
    }
</script>

<style lang="postcss" scoped>
    @import '../../css/conf';

    .plugin-item {
        position: relative;
        display: flex;
        flex-direction: row;
        align-items: center;
        width: 240px;
        height: 42px;
        margin: 0 0 11px 0;
        background-color: white;
        border-radius: 2px;
        font-size: 14px;
        transition: all .4s ease-in-out;
        border: 1px solid #c3cdd7;
        cursor: pointer;
        .plugin-name {
            flex: 1;
            max-width: 148px;
            color: #7b7d8a;
        }
        .plugin-time {
            margin: 0 0px 0 2px;
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
    }
</style>
