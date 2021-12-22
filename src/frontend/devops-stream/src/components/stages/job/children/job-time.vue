<script>
    import { coverTimer, convertMStoString } from '@/utils'
    export default {
        name: 'JobTime',
        props: {
            job: Object
        },
        computed: {
            icon () {
                const { job, convertElapsed } = this
                const { vmNames = [], baseOS = '', elements = [] } = job
                let iconProps = {}
                switch (true) {
                    case job.systemElapsed !== undefined || job.elementElapsed !== undefined:
                        const systemElapsed = convertElapsed(job.systemElapsed)
                        const elementElapsed = convertElapsed(job.elementElapsed)
                        const elapsedSum = systemElapsed + elementElapsed
                        const lt1Hour = elapsedSum < 36e5

                        return (
                            <i v-bk-tooltips={{ content: `用户耗时：${convertMStoString(elementElapsed)} + 系统耗时： ${convertMStoString(systemElapsed)}` }}>{lt1Hour ? coverTimer(elapsedSum) : '>1h'}</i>
                        )
                    case job.isError:
                        iconProps = {
                            class: 'bk-icon icon-exclamation-triangle-shape is-danger'
                        }
                        break
                    case job['@type'] === 'vmBuild':
                        iconProps = {
                            class: `bk-icon icon-${baseOS.toLowerCase()}`,
                            title: vmNames.join(',')
                        }
                        break
                    case job['@type'] === 'normal':
                        iconProps = {
                            class: 'bk-icon icon-none'
                        }
                        break
                    case job['@type'] === 'trigger':
                        return <i>{elements.length} 个</i>
                }
                return <i {...iconProps}></i>
            }
        },
        methods: {
            convertElapsed (val) {
                if (val === undefined) {
                    return 0
                } else {
                    return parseInt(val)
                }
            }
        },
        render (h) {
            return (
                <span class='job-type'>
                    {this.icon}
                </span>
            )
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .job-type {
        font-size: 12px;
        margin-right: 12px;
        font-style: normal;
        .devops-icon {
            font-size: 18px;
            &.icon-exclamation-triangle-shape {
                font-size: 14px;
                &.is-danger {
                    color: $dangerColor;
                }
            }
        }
        i {
            font-style: normal;
        }
    }
</style>
