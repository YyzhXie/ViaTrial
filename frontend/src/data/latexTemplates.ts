// 公式模板字典：按分类组织，点击后插入到公式编辑器源码区。
// 模板中的 `#1`、`#2` 等为占位符，插入时会被展开为可编辑的空花括号 `{}`。
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
      { name: '分式', latex: '\\frac{#1}{#2}' },
      { name: '大分式', latex: '\\dfrac{#1}{#2}' },
      { name: '小分式', latex: '\\tfrac{#1}{#2}' },
      { name: '根式', latex: '\\sqrt{#1}' },
      { name: 'n 次根式', latex: '\\sqrt[#1]{#2}' },
    ],
  },
  {
    name: '上下标与修饰',
    templates: [
      { name: '上标', latex: 'x^{#1}' },
      { name: '下标', latex: 'x_{#1}' },
      { name: '上下标', latex: 'x_{#1}^{#2}' },
      { name: '帽子', latex: '\\hat{#1}' },
      { name: '横线', latex: '\\bar{#1}' },
      { name: '向量', latex: '\\vec{#1}' },
      { name: '点', latex: '\\dot{#1}' },
      { name: '双点', latex: '\\ddot{#1}' },
      { name: '波浪线', latex: '\\tilde{#1}' },
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
      { name: '求和', latex: '\\sum_{#1}^{#2}' },
      { name: '累乘', latex: '\\prod_{#1}^{#2}' },
      { name: '积分', latex: '\\int_{#1}^{#2}' },
      { name: '不定积分', latex: '\\int #1\\,dx' },
      { name: '极限', latex: '\\lim_{#1 \\to #2}' },
      { name: '偏导', latex: '\\frac{\\partial #1}{\\partial #2}' },
    ],
  },
  {
    name: '括号与函数',
    templates: [
      { name: '圆括号', latex: '\\left( #1 \\right)' },
      { name: '方括号', latex: '\\left[ #1 \\right]' },
      { name: '花括号', latex: '\\left\\{ #1 \\right\\}' },
      { name: '绝对值', latex: '\\left| #1 \\right|' },
      { name: '正弦', latex: '\\sin #1' },
      { name: '余弦', latex: '\\cos #1' },
      { name: '正切', latex: '\\tan #1' },
      { name: '对数', latex: '\\log #1' },
      { name: '自然对数', latex: '\\ln #1' },
      { name: '指数', latex: 'e^{#1}' },
    ],
  },
  {
    name: '矩阵与多行',
    templates: [
      { name: '圆括号矩阵', latex: '\\begin{pmatrix} #1 & #2 \\\\ #3 & #4 \\end{pmatrix}' },
      { name: '方括号矩阵', latex: '\\begin{bmatrix} #1 & #2 \\\\ #3 & #4 \\end{bmatrix}' },
      { name: '行列式', latex: '\\begin{vmatrix} #1 & #2 \\\\ #3 & #4 \\end{vmatrix}' },
      { name: '分段函数', latex: '\\begin{cases} #1, & #2 \\\\ #3, & #4 \\end{cases}' },
    ],
  },
  {
    name: '逻辑与几何',
    templates: [
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
      { name: '文本', latex: '\\text{#1}' },
      { name: '小空格', latex: '\\,' },
      { name: '中空格', latex: '\\quad' },
      { name: '大空格', latex: '\\qquad' },
    ],
  },
]
