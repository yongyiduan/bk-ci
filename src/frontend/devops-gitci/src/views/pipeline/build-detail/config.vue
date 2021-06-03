<template>
    <article class="detail-config">
        <bk-tab :active.sync="active">
            <bk-tab-panel
                v-for="(panel, index) in panels"
                v-bind="panel"
                :key="index">
            </bk-tab-panel>
        </bk-tab>

        <code-section :code="ymlObj[active]" limit-height :height="`${height}px`"></code-section>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { pipelines } from '@/http'
    import codeSection from '@/components/code-section'

    export default {
        components: {
            codeSection
        },

        data () {
            return {
                panels: [
                    { label: 'Original YAML', name: 'originYaml' },
                    { label: 'Parsed YAML', name: 'parsedYaml' }
                ],
                active: 'originYaml',
                ymlObj: {}
            }
        },

        computed: {
            ...mapState(['projectId', 'appHeight']),

            height () {
                return this.appHeight - 267
            }
        },

        created () {
            this.getYaml()
        },

        methods: {
            getYaml () {
                pipelines.getPipelineBuildYaml(this.projectId, this.$route.params.buildId).then((res) => {
                    this.ymlObj = res
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .detail-config {
        overflow: hidden;
        /deep/ .bk-tab-section {
            padding: 0;
            border: none;
        }
    }
</style>
