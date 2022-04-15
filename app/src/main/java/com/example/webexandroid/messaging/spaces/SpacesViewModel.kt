package com.example.webexandroid.messaging.spaces

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.webexandroid.BaseViewModel
import com.example.webexandroid.WebexRepository
import com.example.webexandroid.messaging.spaces.members.MembershipModel
import com.example.webexandroid.messaging.spaces.members.MembershipRepository
import com.example.webexandroid.messaging.teams.TeamsRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

class SpacesViewModel(private val spacesRepo: SpacesRepository,
                      private val membershipRepo: MembershipRepository,
                      private val messagingRepo: TeamsRepository, private val webexRepository: WebexRepository) : BaseViewModel() {
    private val _spaces = MutableLiveData<List<SpaceModel>>()
    val spaces: LiveData<List<SpaceModel>> = _spaces

    private val _readStatusList = MutableLiveData<List<SpaceReadStatusModel>>()
    val readStatusList: LiveData<List<SpaceReadStatusModel>> = _readStatusList

    private val _addSpaces = MutableLiveData<SpaceModel>()
    val addSpaces: LiveData<SpaceModel> = _addSpaces

    private val _addID = MutableLiveData<String>()
    val addID: LiveData<String> get()= _addID

    private val _spaceMeetingInfo = MutableLiveData<SpaceMeetingInfoModel>()
    val spaceMeetingInfo: LiveData<SpaceMeetingInfoModel> = _spaceMeetingInfo

    private val _spaceError = MutableLiveData<String>()
    val spaceError: LiveData<String> = _spaceError

    private val _createMemberData = MutableLiveData<MembershipModel>()
    val createMemberData: LiveData<MembershipModel> = _createMemberData

    private val _markSpaceRead = MutableLiveData<Boolean>()
    val markSpaceRead: LiveData<Boolean> = _markSpaceRead

    private val _deleteSpace = MutableLiveData<String>()
    val deleteSpace: LiveData<String> = _deleteSpace

    private val _spaceEventLiveData = MutableLiveData<Pair<WebexRepository.SpaceEvent, Any?>>()

    private val addOnCallSuffix = " (On Call)"

    init {
        webexRepository._spaceEventLiveData = _spaceEventLiveData
    }

    override fun onCleared() {
        webexRepository.clearSpaceData()
    }

    private fun getSpacesWithActiveCalls() {
        val allSpaces = arrayListOf<SpaceModel>()
        spacesRepo.listSpacesWithActiveCalls().observeOn(AndroidSchedulers.mainThread()).subscribe({ spaceIds ->
            spaces.value?.forEach { space ->
                if(spaceIds.contains(space.id)) {
                    val tempSpace = SpaceModel(space.id, space.title + addOnCallSuffix, space.spaceType, space.isLocked, space.lastActivity, space.created, space.teamId, space.sipAddress)
                    allSpaces.add(tempSpace)
                } else {
                    allSpaces.add(space)
                }
            }
            _spaces.postValue(allSpaces)
        }) { _spaces.postValue(spaces.value)}.autoDispose()
    }

    fun getSpaceEvent() = webexRepository._spaceEventLiveData

    fun getSpacesList(maxSpaces: Int) {
        spacesRepo.fetchSpacesList(null, maxSpaces).observeOn(AndroidSchedulers.mainThread()).subscribe({ spacesList ->
            _spaces.postValue(spacesList)
            getSpacesWithActiveCalls()
        }, { _spaces.postValue(emptyList()) }).autoDispose()
    }

    fun addSpace(title: String, teamId: String?) {
        spacesRepo.addSpace(title, teamId).observeOn(AndroidSchedulers.mainThread()).subscribe({ createdSpace ->
            //Log.e("created",createdSpace.toString())
            _addID.postValue(createdSpace.id)
        }, { _addID.postValue(null) }).autoDispose()

    }
    fun getID(): LiveData<String?>? {
        return _addID
    }


    fun getSpaceReadStatusList(maxSpaces: Int) {
        spacesRepo.fetchSpaceReadStatusList(maxSpaces).observeOn(AndroidSchedulers.mainThread()).subscribe({ listReadStatus ->
            _readStatusList.postValue(listReadStatus)
        }, { _readStatusList.postValue(null) }).autoDispose()
    }

    fun updateSpace(spaceId: String, spaceName: String) {
        spacesRepo.updateSpace(spaceId, spaceName).observeOn(AndroidSchedulers.mainThread()).subscribe({
            //Log.d(SpacesViewModel::class.java.simpleName, "Space title is updated")
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun delete(spaceId: String) {
        spacesRepo.delete(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _deleteSpace.postValue(spaceId)
        }, {error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun getMeetingInfo(spaceId: String) {
        spacesRepo.getMeetingInfo(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({ meetingInfo ->
            //Log.e("MeetingInfo",meetingInfo.toString())
            _spaceMeetingInfo.postValue(meetingInfo)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun createMembershipWithId(spaceId: String, personId: String, isModerator: Boolean = false) {
        membershipRepo.createMembershipWithId(spaceId, personId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
           // Log.e("memebership created",membership.toString())
            _createMemberData.postValue(membership)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun createMembershipWithEmailId(spaceId: String, emailId: String, isModerator: Boolean = false) {
        membershipRepo.createMembershipWithEmail(spaceId, emailId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _createMemberData.postValue(membership)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun markSpaceRead(spaceId: String) {
        messagingRepo.markMessageAsRead(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({ success ->
            _markSpaceRead.postValue(success)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    private fun refreshSpaces() {
        getSpacesList(0)
    }
}

