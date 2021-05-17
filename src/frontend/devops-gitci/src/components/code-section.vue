<template>
    <section :class="[{ 'max-height': limitHeight }, 'ci-yml']" :style="styleVar"></section>
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
            limitHeight: {
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
            },
            height: {
                type: String,
                default: '400px'
            }
        },

        data () {
            return {
                codeMirrorCon: {
                    lineNumbers: true,
                    lineWrapping: true,
                    tabMode: 'indent',
                    mode: 'yaml',
                    theme: '3024-night',
                    cursorBlinkRate: this.cursorBlinkRate,
                    readOnly: this.readOnly,
                    autoRefresh: true,
                    autofocus: false
                },
                codeEditor: undefined
            }
        },

        computed: {
            styleVar () {
                return {
                    '--code-editor-height': this.height
                }
            }
        },

        watch: {
            code () {
                this.codeEditor.setValue(this.code || '')
            }
        },

        mounted () {
            this.initCodeMirror()
        },

        beforeDestroy () {
            this.codeEditor = undefined
        },

        methods: {
            initCodeMirror () {
                const ele = document.querySelector('.ci-yml')
                if (this.codeEditor) ele.innerHTML = ''
                this.codeEditor = CodeMirror(ele, this.codeMirrorCon)
                this.codeEditor.setValue(this.code || '')
                this.codeEditor.on('change', this.changeValue)
            },

            changeValue (instance) {
                const value = instance.getValue()
                this.$emit('update:code', value)
            },

            getValue () {
                return this.codeEditor.getValue()
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .ci-yml {
        background: black;
    }

    .max-height {
        height: var(--code-editor-height);
        /deep/ .CodeMirror-scroll {
            height: var(--code-editor-height);
        }
        /deep/ .CodeMirror {
            max-height: var(--code-editor-height);
            padding: 0 10px;
        }
    }

    /deep/ .CodeMirror {
        font-family: Consolas, "Courier New", monospace;
        line-height: 1.5;
        margin-bottom: 20px;
        padding: 10px;
        height: auto;
    }
    /deep/ .CodeMirror {
        min-height: 300px;
        height: auto;
        .CodeMirror-scroll {
            min-height: 300px;
        }
    }
</style>
