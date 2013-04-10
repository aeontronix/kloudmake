require "java"
java_import com.kloudtek.systyrant.context.AbstractAction

class Action < AbstractAction

end

def newres(type, id=nil, attrs=nil, parent=nil)
  $strm.create(type, id, attrs, parent)
end
