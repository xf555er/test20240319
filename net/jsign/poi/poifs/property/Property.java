package net.jsign.poi.poifs.property;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import net.jsign.poi.hpsf.ClassID;
import net.jsign.poi.util.ByteField;
import net.jsign.poi.util.IntegerField;
import net.jsign.poi.util.ShortField;

public abstract class Property implements Child {
   private String _name;
   private ShortField _name_size;
   private ByteField _property_type;
   private ByteField _node_color;
   private IntegerField _previous_property;
   private IntegerField _next_property;
   private IntegerField _child_property;
   private ClassID _storage_clsid;
   private IntegerField _user_flags;
   private IntegerField _seconds_1;
   private IntegerField _days_1;
   private IntegerField _seconds_2;
   private IntegerField _days_2;
   private IntegerField _start_block;
   private IntegerField _size;
   private byte[] _raw_data;
   private int _index;
   private Child _next_child;
   private Child _previous_child;

   protected Property() {
      this._raw_data = new byte[128];
      Arrays.fill(this._raw_data, (byte)0);
      this._name_size = new ShortField(64);
      this._property_type = new ByteField(66);
      this._node_color = new ByteField(67);
      this._previous_property = new IntegerField(68, -1, this._raw_data);
      this._next_property = new IntegerField(72, -1, this._raw_data);
      this._child_property = new IntegerField(76, -1, this._raw_data);
      this._storage_clsid = new ClassID(this._raw_data, 80);
      this._user_flags = new IntegerField(96, 0, this._raw_data);
      this._seconds_1 = new IntegerField(100, 0, this._raw_data);
      this._days_1 = new IntegerField(104, 0, this._raw_data);
      this._seconds_2 = new IntegerField(108, 0, this._raw_data);
      this._days_2 = new IntegerField(112, 0, this._raw_data);
      this._start_block = new IntegerField(116);
      this._size = new IntegerField(120, 0, this._raw_data);
      this._index = -1;
      this.setName("");
      this.setNextChild((Child)null);
      this.setPreviousChild((Child)null);
   }

   protected Property(int index, byte[] array, int offset) {
      this._raw_data = Arrays.copyOfRange(array, offset, offset + 128);
      this._name_size = new ShortField(64, this._raw_data);
      this._property_type = new ByteField(66, this._raw_data);
      this._node_color = new ByteField(67, this._raw_data);
      this._previous_property = new IntegerField(68, this._raw_data);
      this._next_property = new IntegerField(72, this._raw_data);
      this._child_property = new IntegerField(76, this._raw_data);
      this._storage_clsid = new ClassID(this._raw_data, 80);
      this._user_flags = new IntegerField(96, 0, this._raw_data);
      this._seconds_1 = new IntegerField(100, this._raw_data);
      this._days_1 = new IntegerField(104, this._raw_data);
      this._seconds_2 = new IntegerField(108, this._raw_data);
      this._days_2 = new IntegerField(112, this._raw_data);
      this._start_block = new IntegerField(116, this._raw_data);
      this._size = new IntegerField(120, this._raw_data);
      this._index = index;
      int name_length = this._name_size.get() / 2 - 1;
      if (name_length < 1) {
         this._name = "";
      } else {
         char[] char_array = new char[name_length];
         int name_offset = 0;

         for(int j = 0; j < name_length; ++j) {
            char_array[j] = (char)(new ShortField(name_offset, this._raw_data)).get();
            name_offset += 2;
         }

         this._name = new String(char_array, 0, name_length);
      }

      this._next_child = null;
      this._previous_child = null;
   }

   public void writeData(OutputStream stream) throws IOException {
      stream.write(this._raw_data);
   }

   public void setStartBlock(int startBlock) {
      this._start_block.set(startBlock, this._raw_data);
   }

   public int getStartBlock() {
      return this._start_block.get();
   }

   public int getSize() {
      return this._size.get();
   }

   public String getName() {
      return this._name;
   }

   public abstract boolean isDirectory();

   public ClassID getStorageClsid() {
      return this._storage_clsid;
   }

   protected void setName(String name) {
      char[] char_array = name.toCharArray();
      int limit = Math.min(char_array.length, 31);
      this._name = new String(char_array, 0, limit);
      short offset = 0;

      int j;
      for(j = 0; j < limit; ++j) {
         new ShortField(offset, (short)char_array[j], this._raw_data);
         offset = (short)(offset + 2);
      }

      while(j < 32) {
         new ShortField(offset, (short)0, this._raw_data);
         offset = (short)(offset + 2);
         ++j;
      }

      this._name_size.set((short)((limit + 1) * 2), this._raw_data);
   }

   protected void setPropertyType(byte propertyType) {
      this._property_type.set(propertyType, this._raw_data);
   }

   protected void setNodeColor(byte nodeColor) {
      this._node_color.set(nodeColor, this._raw_data);
   }

   protected void setChildProperty(int child) {
      this._child_property.set(child, this._raw_data);
   }

   protected int getChildIndex() {
      return this._child_property.get();
   }

   protected void setSize(int size) {
      this._size.set(size, this._raw_data);
   }

   protected void setIndex(int index) {
      this._index = index;
   }

   protected int getIndex() {
      return this._index;
   }

   protected abstract void preWrite();

   int getNextChildIndex() {
      return this._next_property.get();
   }

   int getPreviousChildIndex() {
      return this._previous_property.get();
   }

   static boolean isValidIndex(int index) {
      return index != -1;
   }

   public void setNextChild(Child child) {
      this._next_child = child;
      this._next_property.set(child == null ? -1 : ((Property)child).getIndex(), this._raw_data);
   }

   public void setPreviousChild(Child child) {
      this._previous_child = child;
      this._previous_property.set(child == null ? -1 : ((Property)child).getIndex(), this._raw_data);
   }
}
