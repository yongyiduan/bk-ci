<template>
    <bk-input :value="paramValue[paramKey]" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width"></bk-input>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
