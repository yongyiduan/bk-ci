<template>
    <article class="detail-config">
        <bk-tab :active.sync="active">
            <bk-tab-panel
                v-for="(panel, index) in panels"
                v-bind="panel"
                :key="index">
            </bk-tab-panel>
        </bk-tab>

        <code-section :code="yml" limit-height height="calc(100% - 50px)"></code-section>
    </article>
</template>

<script>
    import codeSection from '@/components/code-section'

    export default {
        components: {
            codeSection
        },

        data () {
            return {
                panels: [
                    { label: 'Parsed YAML', name: 'parsed' },
                    { label: 'Original YAML', name: 'original' }
                ],
                active: 'parsed',
                yml: ''
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .detail-config {
        /deep/ .bk-tab-section {
            padding: 0;
            border: none;
        }
    }
</style>
