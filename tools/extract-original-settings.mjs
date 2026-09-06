// Read-only extraction from JADX sources. Generated data is reviewed and added using apply_patch.
import {readFileSync} from 'node:fs';
import {createHash} from 'node:crypto';
const root=process.argv[2];
if (!root) throw new Error('Pass the original JADX sources directory');
const read=relative=>readFileSync(root+'/'+relative,'utf8');
function block(source,start,open='(',close=')') {
  let depth=0,quote=false,escape=false;
  for(let i=start;i<source.length;i++) {
    const c=source[i];
    if(quote) { if(escape) escape=false; else if(c==='\\') escape=true; else if(c==='"') quote=false; continue; }
    if(c==='"') { quote=true; continue; }
    if(c===open) depth++;
    if(c===close && --depth===0) return {text:source.slice(start+1,i),end:i+1};
  }
  throw new Error('Unbalanced source');
}
function args(source) {
  let depth=0,quote=false,escape=false,start=0,result=[];
  for(let i=0;i<source.length;i++) {
    let c=source[i];
    if(quote) { if(escape) escape=false; else if(c==='\\') escape=true; else if(c==='"') quote=false; continue; }
    if(c==='"') quote=true;
    else if('({['.includes(c)) depth++;
    else if(')}]'.includes(c)) depth--;
    else if(c===',' && !depth) {result.push(source.slice(start,i).trim());start=i+1;}
  }
  result.push(source.slice(start).trim());return result;
}
const constants={BLUE_DEFAULT:255,RED_DEFAULT:189,GREEN_DEFAULT:140};
function literal(s) {
  if(s==='null') return null;
  if(s==='true' || s==='false') return s==='true';
  if(/^"(?:[^"\\]|\\.)*"$/.test(s)) return JSON.parse(s);
  if(/^-?\d+(?:\.\d+)?[dDfFlL]?$/.test(s)) return Number(s.replace(/[dDfFlL]$/,''));
  const name=s.split('.').at(-1); if(name in constants) return constants[name];
  throw new Error('Nonliteral: '+s);
}
const registry=read('wtf/tatp/meowtils/module/RegisterModule.java');
const classes=[...registry.matchAll(/new (wtf\.tatp\.meowtils\.module\.[\w.]+)\(\)/g)].map(m=>m[1]);
const output=[];
for(const clazz of classes) {
  const source=read(clazz.replaceAll('.','/')+'.java');
  for (const m of source.matchAll(/static final [\w.]+ (\w+) = ([^;]+);/g)) {
    try { constants[m[1]]=literal(m[2]); } catch { }
  }
  const simple=clazz.split('.').at(-1);
  const start=source.indexOf('public '+simple+'()');
  const body=block(source,source.indexOf('{',start),'{','}').text;
  const superArgs=args(block(body,body.indexOf('super(')+5).text);
  const defaults={},defaultExpressions={};
  for(const m of body.matchAll(/this\.(\w+)\s*=\s*([^;]+);/g)) {
    try{defaults[m[1]]=literal(m[2].trim());}catch{defaultExpressions[m[1]]=m[2].trim();}
  }
  const links={};
  for(const m of body.matchAll(/ColorLink (\w+) = new [\w.]+ColorLink\(/g)) {
    const a=args(block(body,m.index+m[0].length-1).text);links[m[1]]=a.slice(0,3).map(literal);
  }
  function controls(text) {
    const result=[];
    const re=/\badd(Toggle|Check|Slider|Mode|Text|Bind|Color|Saturation|Brightness|Opacity|Button|Expand)\(new [\w.]+Value\(/g;
    let m;
    while((m=re.exec(text))) {
      const parsed=block(text,m.index+m[0].length-1),a=args(parsed.text),type=m[1];
      re.lastIndex=parsed.end;
      const v={type};
      if(type==='Saturation' || type==='Brightness') { v.name=type;v.link=links[a[0]]; }
      else { v.name=literal(a[0]); }
      if(type==='Toggle' || type==='Check' || type==='Bind' || type==='Opacity') v.config=literal(a[1]);
      if(type==='Text') { v.config=literal(a.at(-2));v.description=a.length===4?literal(a[1]):''; }
      if(type==='Slider') {v.min=literal(a[1]);v.max=literal(a[2]);v.increment=literal(a[3]);v.unit=literal(a[4]);v.config=literal(a[5]);}
      if(type==='Mode') {v.modes=args(block(a[1],a[1].indexOf('(')).text).map(literal);v.config=literal(a[2]);}
      if(type==='Color') v.link=links[a[1]];
      if(type==='Expand') v.children=controls(a[1]);
      if(type==='Button') {v.textScale=literal(a[1]);v.action=a.slice(2).join(', ');}
      if(v.config && Object.hasOwn(defaults,v.config)) v.default=defaults[v.config];
      if(v.config && Object.hasOwn(defaultExpressions,v.config)) throw new Error(clazz+'.'+v.config+': '+defaultExpressions[v.config]);
      if(['Color','Saturation','Brightness'].includes(type) && !v.link) throw new Error('Missing color link '+clazz);
      result.push(v);
    }
    return result;
  }
  const tooltipMatch=/\btooltip\(/.exec(body);
  const tooltip=tooltipMatch?literal(block(body,tooltipMatch.index+7).text):'';
  output.push({class:clazz,sourceHash:createHash('sha256').update(source).digest('hex'),name:literal(superArgs[0]),category:superArgs[1].split('.').at(-1),alwaysEnabled:superArgs[2]==='true',tag:/ModuleTag\.(\w+)/.exec(body)?.[1]??null,tooltip,defaults,values:controls(body)});
}
console.log(JSON.stringify(output));
