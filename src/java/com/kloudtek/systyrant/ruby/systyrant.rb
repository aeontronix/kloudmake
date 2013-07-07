require "java"
java_import com.kloudtek.systyrant.AbstractAction

module SysTyrant
  class Action < AbstractAction

  end

  def SysTyrant.create(type, id=nil, attrs=nil, parent=nil)
    $strm.create(type, id, attrs, parent)
  end
end
