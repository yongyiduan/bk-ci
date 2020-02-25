<template>
    <section class="store-code" v-bkloading="{ isLoading }"></section>
</template>

<script>
    import CodeMirror from 'codemirror'
    import 'codemirror/addon/display/autorefresh'
    import 'codemirror/mode/yaml/yaml'
    import 'codemirror/lib/codemirror.css'
    import 'codemirror/theme/3024-night.css'

    export default {
        props: {
            readOnly: {
                type: Boolean,
                default: true
            },
            cursorBlinkRate: {
                type: Number,
                default: 0
            },
            code: {
                type: String,
                require: true
            }
        },

        data () {
            return {
                codeMirrorCon: {
                    lineNumbers: true,
                    tabMode: 'indent',
                    mode: 'yaml',
                    theme: '3024-night',
                    height: '400px',
                    cursorBlinkRate: this.cursorBlinkRate,
                    readOnly: this.readOnly,
                    autoRefresh: true,
                    autofocus: true
                },
                codeEditor: undefined
            }
        },

        watch: {
            code () {
                this.initCodeMirror()
            }
        },

        mounted () {
            this.initCodeMirror()
        },

        methods: {
            initCodeMirror () {
                const ele = document.querySelector('.store-code')
                if (this.codeEditor) ele.innerHTML = ''
                this.codeEditor = CodeMirror(ele, this.codeMirrorCon)
                this.codeEditor.setValue(this.code || '')
            },

            getValue () {
                return this.codeEditor.getValue()
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .store-code {
        height: 400px;
        background: black;
    }

    /deep/ .CodeMirror {
        font-family: Consolas, "Courier New", monospace;
        line-height: 1.5;
        margin-bottom: 20px;
        padding: 10px;
        height: auto;
        .CodeMirror-scroll {
            height: 400px;
        }
    }
    /deep/ .CodeMirror {
        min-height: 300px;
        height: auto;
    }
</style>
