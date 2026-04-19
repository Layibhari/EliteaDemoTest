#!/bin/bash
set -e

TASK_ID=${1:-"TASK-001"}
DESC=${2:-"feature-name"}
BRANCH="feature/${TASK_ID}-${DESC}"

echo "🚀 开始初始化/同步工作流..."

if [ ! -d ".git" ]; then
  echo "❌ 错误：请在项目根目录执行此脚本"
  exit 1
fi

if ! git remote | grep -q upstream; then
  git remote add upstream https://github.com/spring-projects/spring-petclinic.git
  echo "✅ 已添加 upstream 远程仓库"
fi

git fetch upstream

if ! git branch --list | grep -q "develop"; then
  git checkout -b develop upstream/main
  echo "✅ 已创建 develop 分支"
else
  git checkout develop
  git merge --ff-only upstream/main 2>/dev/null || git rebase upstream/main
  echo "✅ 已同步 develop 到最新"
fi

if git branch --list | grep -q "${BRANCH}"; then
  echo "⚠️ 发现旧分支 ${BRANCH}，正在删除本地副本..."
  git branch -D ${BRANCH}
fi

git checkout -b ${BRANCH}
echo "✅ 完成！当前分支: ${BRANCH}"
echo "📝 下一步: 打开 IDE/Trae，开始编码。提交前记得 ./mvnw clean test"
