<template>
    <bk-radio-group class="stage-review-radio-group" v-model="stageControl.manualTrigger">
        <bk-radio :disabled="true" :value="false">Auto</bk-radio>
        <bk-radio :disabled="true" :value="true" style="marginLeft:55px">Manual</bk-radio>
    </bk-radio-group>
</template>

<script>
    export default {
        props: {
            stageControl: {
                type: Object,
                default: () => ({})
            }
        }
    }
</script>
