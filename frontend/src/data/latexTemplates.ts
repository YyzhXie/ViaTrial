// 公式模板字典：按分类组织，点击后插入到公式编辑器源码区。
// 每个模板的 `latex` 既是插入内容，也是按钮上的可视化预览内容（可直接被 KaTeX 渲染）。
// 模板内容需对齐 KaTeX 支持的命令子集。

export interface LatexTemplate {
  name: string
  latex: string
}

export interface LatexTemplateCategory {
  name: string
  templates: LatexTemplate[]
}

export const latexTemplateCategories: LatexTemplateCategory[] = [
  {
    name: '分数与根式',
    templates: [
      { name: '分式', latex: '\\frac{1}{2}' },
      { name: '大分式', latex: '\\dfrac{1}{2}' },
      { name: '小分式', latex: '\\tfrac{1}{2}' },
      { name: '根式', latex: '\\sqrt{x}' },
      { name: 'n 次根式', latex: '\\sqrt[n]{x}' },
    ],
  },
  {
    name: '上下标与修饰',
    templates: [
      { name: '上标', latex: 'x^{2}' },
      { name: '下标', latex: 'x_{i}' },
      { name: '上下标', latex: 'x_{i}^{2}' },
      { name: '帽子', latex: '\\hat{x}' },
      { name: '横线', latex: '\\bar{x}' },
      { name: '向量', latex: '\\vec{x}' },
      { name: '点', latex: '\\dot{x}' },
      { name: '双点', latex: '\\ddot{x}' },
      { name: '波浪线', latex: '\\tilde{x}' },
    ],
  },
  {
    name: '运算与关系',
    templates: [
      { name: '加减', latex: '\\pm' },
      { name: '乘', latex: '\\times' },
      { name: '除', latex: '\\div' },
      { name: '点乘', latex: '\\cdot' },
      { name: '小于等于', latex: '\\leq' },
      { name: '大于等于', latex: '\\geq' },
      { name: '不等于', latex: '\\neq' },
      { name: '约等于', latex: '\\approx' },
      { name: '无穷', latex: '\\infty' },
      { name: '属于', latex: '\\in' },
      { name: '并集', latex: '\\cup' },
      { name: '交集', latex: '\\cap' },
      { name: '子集', latex: '\\subseteq' },
      { name: '空集', latex: '\\emptyset' },
      { name: '任意', latex: '\\forall' },
      { name: '存在', latex: '\\exists' },
    ],
  },
  {
    name: '希腊字母',
    templates: [
      { name: 'α', latex: '\\alpha' },
      { name: 'β', latex: '\\beta' },
      { name: 'γ', latex: '\\gamma' },
      { name: 'δ', latex: '\\delta' },
      { name: 'ε', latex: '\\epsilon' },
      { name: 'θ', latex: '\\theta' },
      { name: 'λ', latex: '\\lambda' },
      { name: 'μ', latex: '\\mu' },
      { name: 'π', latex: '\\pi' },
      { name: 'ρ', latex: '\\rho' },
      { name: 'σ', latex: '\\sigma' },
      { name: 'φ', latex: '\\phi' },
      { name: 'ω', latex: '\\omega' },
      { name: 'Γ', latex: '\\Gamma' },
      { name: 'Δ', latex: '\\Delta' },
      { name: 'Θ', latex: '\\Theta' },
      { name: 'Λ', latex: '\\Lambda' },
      { name: 'Σ', latex: '\\Sigma' },
      { name: 'Φ', latex: '\\Phi' },
      { name: 'Ω', latex: '\\Omega' },
    ],
  },
  {
    name: '求和与积分',
    templates: [
      { name: '求和', latex: '\\sum_{i=1}^{n}' },
      { name: '累乘', latex: '\\prod_{i=1}^{n}' },
      { name: '积分', latex: '\\int_{a}^{b}' },
      { name: '不定积分', latex: '\\int x\\,dx' },
      { name: '极限', latex: '\\lim_{x \\to \\infty}' },
      { name: '偏导', latex: '\\frac{\\partial f}{\\partial x}' },
    ],
  },
  {
    name: '括号与函数',
    templates: [
      { name: '圆括号', latex: '\\left( x \\right)' },
      { name: '方括号', latex: '\\left[ x \\right]' },
      { name: '花括号', latex: '\\left\\{ x \\right\\}' },
      { name: '绝对值', latex: '\\left| x \\right|' },
      { name: '正弦', latex: '\\sin x' },
      { name: '余弦', latex: '\\cos x' },
      { name: '正切', latex: '\\tan x' },
      { name: '对数', latex: '\\log x' },
      { name: '自然对数', latex: '\\ln x' },
      { name: '指数', latex: 'e^{x}' },
    ],
  },
  {
    name: '矩阵与多行',
    templates: [
      { name: '圆括号矩阵', latex: '\\begin{pmatrix} 1 & 2 \\\\ 3 & 4 \\end{pmatrix}' },
      { name: '方括号矩阵', latex: '\\begin{bmatrix} 1 & 2 \\\\ 3 & 4 \\end{bmatrix}' },
      { name: '行列式', latex: '\\begin{vmatrix} 1 & 2 \\\\ 3 & 4 \\end{vmatrix}' },
      { name: '分段函数', latex: '\\begin{cases} x, & x > 0 \\\\ -x, & x \\leq 0 \\end{cases}' },
    ],
  },
  {
    name: '逻辑与几何',
    templates: [
      { name: '且（合取）', latex: '\\land' },
      { name: '或（析取）', latex: '\\lor' },
      { name: '非（否定）', latex: '\\neg' },
      { name: '异或', latex: '\\oplus' },
      { name: '右箭头', latex: '\\to' },
      { name: '推出', latex: '\\Rightarrow' },
      { name: '左箭头', latex: '\\leftarrow' },
      { name: '双箭头', latex: '\\Leftrightarrow' },
      { name: '蕴含', latex: '\\implies' },
      { name: '等价', latex: '\\iff' },
      { name: '角', latex: '\\angle' },
      { name: '三角形', latex: '\\triangle' },
      { name: '垂直', latex: '\\perp' },
      { name: '平行', latex: '\\parallel' },
      { name: '度', latex: '^{\\circ}' },
    ],
  },
  {
    name: '文本与空格',
    templates: [
      { name: '文本', latex: '\\text{示例}' },
      { name: '小空格', latex: '\\,' },
      { name: '中空格', latex: '\\quad' },
      { name: '大空格', latex: '\\qquad' },
    ],
  },
]
