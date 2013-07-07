require "java"
java_import com.kloudtek.systyrant.AbstractTask

module SysTyrant
  class Task < AbstractTask

  end

  def SysTyrant.create(type, id=nil, attrs=nil, parent=nil)
    $strm.create(type, id, attrs, parent)
  end
end
