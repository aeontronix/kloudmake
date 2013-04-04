require "java"
java_import com.kloudtek.systyrant.resource.AbstractAction

def newres(type, id=nil, attrs=nil, parent=nil)
  $strm.create(type, id, attrs, parent)
end
