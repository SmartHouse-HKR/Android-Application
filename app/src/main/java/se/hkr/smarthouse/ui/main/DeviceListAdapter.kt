package se.hkr.smarthouse.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_list_item_alarm.view.*
import kotlinx.android.synthetic.main.device_list_item_fan.view.*
import kotlinx.android.synthetic.main.device_list_item_header.view.*
import kotlinx.android.synthetic.main.device_list_item_heater.view.*
import kotlinx.android.synthetic.main.device_list_item_light.view.*
import kotlinx.android.synthetic.main.device_list_item_oven.view.*
import kotlinx.android.synthetic.main.device_list_item_temperature.view.*
import se.hkr.smarthouse.R
import se.hkr.smarthouse.models.Device
import se.hkr.smarthouse.models.getSimpleName

class DeviceListAdapter(
    private val interaction: Interaction
) : RecyclerView.Adapter<DeviceListAdapter.BaseViewHolder<*>>() {
    companion object {
        const val TAG = "AppDebug"
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.topic.split("/").take(2) == newItem.topic.split("/").take(2)
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            try {
                return when (oldItem) {
                    is Device.UnknownDevice -> {
                        return true
                    }
                    is Device.Light -> {
                        newItem as Device.Light
                        newItem.state == oldItem.state
                    }
                    is Device.Temperature -> {
                        newItem as Device.Temperature
                        newItem.temperature == oldItem.temperature
                    }
                    is Device.Voltage -> {
                        newItem as Device.Voltage
                        newItem.voltage == oldItem.voltage
                    }
                    is Device.Oven -> {
                        newItem as Device.Oven
                        newItem.state == oldItem.state
                    }
                    is Device.Fan -> {
                        newItem as Device.Fan
                        newItem.speed == oldItem.speed
                    }
                    is Device.Heater -> {
                        newItem as Device.Heater
                        newItem.state == oldItem.state && newItem.value == oldItem.value
                    }
                    is Device.Alarm -> {
                        newItem as Device.Alarm
                        newItem.active == oldItem.active && newItem.triggered == oldItem.triggered
                    }
                }
            } catch (e: Exception) {
                // If typecast failed, means they are different class anyway, therefore different.
                return false
            }
        }
    }

    private val differ = object : AsyncListDiffer<Device>(this, diffCallback) {
        // Have to override because if the device send to submitList is == to the old device, which
        // is true since we are also updating the local variable whenever there is a change. We get
        // around it by sending a copy of the device which does not return true on the == operation
        // and therefore continues on the method and triggers the notifyDataSetChanged()
        override fun submitList(newList: List<Device>?, commitCallback: Runnable?) {
            super.submitList(newList?.toList(), commitCallback)
        }

        override fun submitList(newList: MutableList<Device>?) {
            super.submitList(newList?.toList())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            Device.Light.IDENTIFIER -> {
                LightViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_light, parent, false
                    ), interaction
                )
            }
            Device.Temperature.IDENTIFIER -> {
                TemperatureViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_temperature, parent, false
                    )
                )
            }
            Device.Voltage.IDENTIFIER -> {
                VoltageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_voltage, parent, false
                    ), interaction
                )
            }
            Device.Oven.IDENTIFIER -> {
                OvenViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_oven, parent, false
                    ), interaction
                )
            }
            Device.Fan.IDENTIFIER -> {
                FanViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_fan, parent, false
                    ), interaction
                )
            }
            Device.Heater.IDENTIFIER -> {
                HeaterViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_heater, parent, false
                    ), interaction
                )
            }
            Device.Alarm.IDENTIFIER -> {
                AlarmViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_alarm, parent, false
                    ), interaction
                )
            }
            else -> {
                UnknownDeviceViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.device_list_item_unknown, parent, false
                    ), interaction
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val device = differ.currentList[position]
        when (holder) {
            is UnknownDeviceViewHolder -> holder.bind(device)
            is LightViewHolder -> holder.bind(device)
            is TemperatureViewHolder -> holder.bind(device)
            is VoltageViewHolder -> holder.bind(device)
            is OvenViewHolder -> holder.bind(device)
            is FanViewHolder -> holder.bind(device)
            is HeaterViewHolder -> holder.bind(device)
            is AlarmViewHolder -> holder.bind(device)
            else -> throw IllegalArgumentException("Illegal bind view holder")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is Device.UnknownDevice -> Device.UnknownDevice.IDENTIFIER
            is Device.Light -> Device.Light.IDENTIFIER
            is Device.Temperature -> Device.Temperature.IDENTIFIER
            is Device.Voltage -> Device.Voltage.IDENTIFIER
            is Device.Oven -> Device.Oven.IDENTIFIER
            is Device.Fan -> Device.Fan.IDENTIFIER
            is Device.Heater -> Device.Heater.IDENTIFIER
            is Device.Alarm -> Device.Alarm.IDENTIFIER
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Device>) {
        differ.submitList(list) { Log.d(TAG, "adapter device submission done") }
    }

    inner class UnknownDeviceViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) = with(item as Device.UnknownDevice) {
            Log.d(TAG, "Binding UnknownDevice viewHolder: $item")
            itemView.text_device_topic.text = item.topic
            itemView.text_device_name.text = item.getSimpleName()
        }
    }

    inner class LightViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) = with(item as Device.Light) {
            Log.d(TAG, "Binding Light viewHolder: $item")
            itemView.text_device_topic.text = item.topic
            itemView.text_device_name.text = item.getSimpleName()
            itemView.switch_light_state.isChecked = item.state
            itemView.switch_light_state.setOnClickListener {
                interaction.onDeviceStateChanged(
                    "${item.topic}/state",
                    if (itemView.switch_light_state.isChecked) "on" else "off"
                )
            }
        }
    }

    inner class TemperatureViewHolder
    constructor(
        itemView: View
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) = with(item as Device.Temperature) {
            Log.d(TAG, "Binding Temperature viewHolder: $item")
            itemView.text_device_topic.text = item.topic
            itemView.text_device_name.text = item.getSimpleName()
            itemView.text_temperature_value.text = item.temperature
            itemView.slider_temperature_value.value = item.temperature.toFloat()
            // Make it not clickable by consuming the touch event and doing nothing with it
            itemView.slider_temperature_value.setOnTouchListener { _, _ -> true }
        }
    }

    inner class VoltageViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) {
            val currentItem = item as Device.Voltage
            Log.d(TAG, "Binding Voltage viewHolder: $currentItem")
            itemView.text_device_topic.text = currentItem.topic
            itemView.text_device_name.text = currentItem.getSimpleName()
            //TODO rest
        }
    }

    inner class OvenViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) {
            val currentItem = item as Device.Oven
            Log.d(TAG, "Binding Oven viewHolder: $currentItem")
            itemView.text_device_topic.text = currentItem.topic
            itemView.text_device_name.text = currentItem.getSimpleName()
            itemView.switch_oven_state.isChecked = currentItem.state
            itemView.switch_oven_state.setOnClickListener {
                interaction.onDeviceStateChanged(
                    "${currentItem.topic}/state",
                    if (itemView.switch_oven_state.isChecked) "on" else "off"
                )
            }
        }
    }

    inner class FanViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) {
            val currentItem = item as Device.Fan
            Log.d(TAG, "Binding Fan viewHolder: $currentItem")
            itemView.text_device_topic.text = currentItem.topic
            itemView.text_device_name.text = currentItem.getSimpleName()
            val itemsArray = itemView.context.resources.getStringArray(R.array.fan_speed_array)
            // This assumes that .speed will always be 0/50/75/100, otherwise just shows 0%
            itemView.spinner_fan_speed.setSelection(
                itemsArray.indexOfFirst { arrayItem ->
                    arrayItem.removeSuffix("%") == currentItem.speed
                }, false
            )
            itemView.spinner_fan_speed.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        pos: Int,
                        id: Long
                    ) {
                        Log.d(TAG, "itemSelected pos: ${itemsArray[pos]}")
                        interaction.onDeviceStateChanged(
                            "${item.topic}/speed",
                            itemsArray[pos].removeSuffix("%")
                        )
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
        }
    }

    inner class HeaterViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) {
            val currentItem = item as Device.Heater
            Log.d(TAG, "Binding Heater viewHolder: $currentItem")
            itemView.text_device_topic.text = currentItem.topic
            itemView.text_device_name.text = currentItem.getSimpleName()
            itemView.switch_heater_activate.isChecked = currentItem.state ?: false
            itemView.switch_heater_activate.setOnClickListener {
                interaction.onDeviceStateChanged(
                    "${currentItem.topic}/state",
                    if (itemView.switch_heater_activate.isChecked) "on" else "off"
                )
            }
            itemView.text_heater_value.text = currentItem.value ?: "0"
            itemView.slider_heater_value.value = currentItem.value?.toFloat() ?: 0f
            itemView.slider_heater_value.setOnChangeListener { _, value ->
                itemView.text_heater_value.text = String.format("%.2f", value)
            }
            itemView.slider_heater_value.setOnTouchListener { _, event ->
                if (event?.action == MotionEvent.ACTION_UP) {
                    interaction.onDeviceStateChanged(
                        "${currentItem.topic}/value",
                        itemView.text_heater_value.text.toString()
                    )
                }
                false
            }
        }
    }

    inner class AlarmViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : BaseViewHolder<Device>(itemView) {
        override fun bind(item: Device) {
            val currentItem = item as Device.Alarm
            Log.d(TAG, "Binding Alarm viewHolder: $currentItem")
            itemView.text_device_topic.text = currentItem.topic
            itemView.text_device_name.text = currentItem.getSimpleName()
            itemView.switch_alarm_activate.isChecked = currentItem.active ?: false
            itemView.switch_alarm_trigger.isChecked = currentItem.triggered ?: false
            itemView.switch_alarm_activate.setOnClickListener {
                interaction.onDeviceStateChanged(
                    "${currentItem.topic}/state",
                    if (itemView.switch_alarm_activate.isChecked) "on" else "off"
                )
            }
        }
    }

    abstract class BaseViewHolder<T>(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    interface Interaction {
        fun onDeviceStateChanged(topic: String, message: String)
    }
}
