from django.shortcuts import render
from django.http import JsonResponse
from .models import DeviceCategory, RepairGuide

def matengenezo_page(request):
    categories = DeviceCategory.objects.all()
    return render(request, 'matengenezo.html', {"categories": categories})


def api_categories(request):
    cats = DeviceCategory.objects.all()
    data = [{"id": c.id, "name": c.name, "icon": c.icon_name} for c in cats]
    return JsonResponse({"categories": data})


def api_guides(request):
    category_id = request.GET.get("category_id")
    query = request.GET.get("q", "").strip()

    guides = RepairGuide.objects.all()
    if category_id:
        guides = guides.filter(category_id=category_id)
    if query:
        guides = guides.filter(title__icontains=query) | guides.filter(symptom__icontains=query)

    data = [{
        "id": g.id,
        "title": g.title,
        "brand": g.brand,
        "symptom": g.symptom,
        "solution_steps": g.solution_steps,
        "difficulty": g.get_difficulty_display(),
        "image": g.image.url if g.image else None,
    } for g in guides[:100]]
    return JsonResponse({"guides": data})
