set echo_path=C:\Temp\schedulers\scheduled_tasks\plan_search\output\output.txt
echo ----------------------------------------------------------------------------------------------------->> %echo_path%
echo start procedure date: %DATE% %TIME% >> %echo_path%
mql C:\Temp\schedulers\scheduled_tasks\plan_search\query.mql >> %echo_path%
echo finished procedure date: %DATE% %TIME% >> %echo_path%
echo ----------------------------------------------------------------------------------------------------->> %echo_path%
pause
