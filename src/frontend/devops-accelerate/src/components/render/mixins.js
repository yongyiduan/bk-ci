export default {
    props: {
        paramEnum: Array,
        paramKey: String,
        isEdit: Boolean,
        paramValue: Object
    },

    computed: {
        displayValue () {
            const value = this.paramValue[this.paramKey]
            const curEnum = this.paramEnum.find((param) => (param.id === +value)) || {}
            return curEnum.name
        }
    },

    methods: {
        changeParamValue (val) {
            const paramValue = JSON.parse(JSON.stringify(this.paramValue))
            paramValue[this.paramKey] = val
            this.$emit('update:paramValue', paramValue)
        }
    }
}
