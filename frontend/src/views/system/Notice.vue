<template>
  <div class="page">
    <h2 class="page-title">通知公告</h2>
    <p class="page-sub">发布平台通知与公告，已发布内容会展示在顶栏铃铛供所有用户查看</p>

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
      <span class="muted" style="margin-left:auto">共 {{ rows.length }} 条，已发布 {{ publishedCount }} 条</span>
    </div>

    <div class="card">
      <el-table :data="rows" size="small" stripe>
        <el-table-column label="标题" min-width="240">
          <template #default="{ row }">
            <el-tag v-if="row.top" size="small" type="danger" effect="plain" style="margin-right:6px">置顶</el-tag>
            <span :class="{ draft: row.status !== 'PUBLISHED' }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }"><el-tag size="small" :type="row.type === 'ANNOUNCE' ? 'warning' : 'info'" effect="plain">{{ row.type === 'ANNOUNCE' ? '公告' : '通知' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag size="small" :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="110" />
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.publishedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="view(row)">查看</el-button>
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" link type="success" size="small" @click="publish(row)">发布</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" maxlength="120" show-word-limit /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button value="NOTICE">通知</el-radio-button>
            <el-radio-button value="ANNOUNCE">公告</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="5" maxlength="2000" show-word-limit /></el-form-item>
        <el-form-item label="置顶"><el-switch v-model="form.top" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button value="PUBLISHED">立即发布</el-radio-button>
            <el-radio-button value="DRAFT">存为草稿</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" :title="current.title" width="520px">
      <div class="view-meta">
        <el-tag size="small" :type="current.type === 'ANNOUNCE' ? 'warning' : 'info'" effect="plain">{{ current.type === 'ANNOUNCE' ? '公告' : '通知' }}</el-tag>
        <span class="muted">{{ current.publisher }} · {{ fmtDateTime(current.publishedAt) }}</span>
      </div>
      <p class="view-body">{{ current.content }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '../../api'
import { fmtDateTime } from '../../utils/date'

const rows = ref([])
const visible = ref(false)
const viewVisible = ref(false)
const editing = ref(false)
const current = ref({})
const form = reactive({ id: null, title: '', content: '', type: 'NOTICE', status: 'PUBLISHED', top: false })

const publishedCount = computed(() => rows.value.filter((r) => r.status === 'PUBLISHED').length)

async function load() { rows.value = await systemApi.notices() }
function openCreate() { editing.value = false; Object.assign(form, { id: null, title: '', content: '', type: 'NOTICE', status: 'PUBLISHED', top: false }); visible.value = true }
function openEdit(row) { editing.value = true; Object.assign(form, row); visible.value = true }
function view(row) { current.value = row; viewVisible.value = true }

async function save() {
  if (!form.title) { ElMessage.warning('请输入标题'); return }
  await systemApi.saveNotice({ ...form })
  ElMessage.success('已保存'); visible.value = false; load()
}
async function publish(row) {
  await systemApi.saveNotice({ ...row, status: 'PUBLISHED' })
  ElMessage.success('已发布'); load()
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.title}」？`, '提示', { type: 'warning' })
  } catch (e) { return }
  await systemApi.removeNotice(row.id); ElMessage.success('已删除'); load()
}

onMounted(load)
</script>

<style scoped>
.draft { color: #9aa2af; }
.view-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.view-body { font-size: 14px; line-height: 1.8; color: #485061; margin: 0; white-space: pre-wrap; }
</style>
