const n=new Set(["http:","https:"]);function s(o){const t=(o||"").trim();if(!t)return"";try{const r=new URL(t),e=r.protocol.toLowerCase();return n.has(e)?r.toString():""}catch{return""}}export{s};
