<template>
  <div class="toolbar">
    <el-radio-group v-model="quick" size="default" @change="onQuick">
      <el-radio-button :value="7">近7天</el-radio-button>
      <el-radio-button :value="14">近14天</el-radio-button>
      <el-radio-button :value="30">近30天</el-radio-button>
    </el-radio-group>
    <el-date-picker
      v-model="range"
      type="daterange"
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      value-format="YYYY-MM-DD"
      size="default"
      @change="onRange"
    />
    <slot />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { lastNDays } from '../utils/date'

const props = defineProps({ initialDays: { type: Number, default: 7 } })
const emit = defineEmits(['change'])
const quick = ref(props.initialDays)
const range = ref(lastNDays(props.initialDays))

function push() {
  // never emit a null/half range — fall back to the quick default if the picker was cleared
  const r = Array.isArray(range.value) && range.value.length === 2 && range.value[0]
    ? range.value
    : lastNDays(quick.value || props.initialDays)
  emit('change', { from: r[0], to: r[1] })
}
function onQuick(days) {
  quick.value = days
  range.value = lastNDays(days)
  push()
}
function onRange(v) {
  if (v && v.length === 2 && v[0]) { quick.value = null; push() }
  else {
    // cleared: restore a valid range and re-select the default so parent stays in sync
    quick.value = props.initialDays
    range.value = lastNDays(props.initialDays)
    push()
  }
}
defineExpose({ push })
</script>
